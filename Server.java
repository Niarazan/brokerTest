
import static java.lang.Thread.sleep;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.list;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

//@ApplicationScoped    
@ServerEndpoint("/test")
public class Server {  
    
  static String stock_quotes[] = {"Microsoft","Google","INTEL"};
  static String stock_quotes1[];
  //static List<String> strings = Arrays.asList("Microsoft","Google","INTEL");
  static double stock_values[] = new double[]{5,3,2};
  static double temp_stock_values[] = new double[]{5,3,2};
  static HashMap<String,String> hm=new HashMap<String,String>(); 
  //static HashMap<String,Object> stock_values=new HashMap<String,Object>(){{
      // put("Microsoft", 20.0); put("Google", 25.0); put("INTEL", 35.0);}};
//  static HashMap<String,Object> temp_stock_values=new HashMap<String,Object>(){{
//       put("Microsoft", 20); put("Google", 25); put("INTEL", 35);}};
  String []stocks_req;
  static int random_value;
  static Random rand = new Random(); 
  static JSONObject q = new JSONObject();
  
 private static Queue<Session> queue = new ConcurrentLinkedQueue<Session>();
 private static Thread firstThread,secondThread ;
 static
 {
  firstThread=new Thread(){
   public void run() {
    DecimalFormat df = new DecimalFormat("#.####");
    while(true)
    {
     double d=2+Math.random();  
     random_value = rand.nextInt(3);
     //Collections.shuffle(strings);
     //stock_values.put(strings.get(random_value), d);
     stock_values[random_value] = d;   
        System.out.println("stock_values"+ stock_values[random_value]);
     
          
     try {
      sleep(5000);
     } catch (InterruptedException e) {      
     }
    }
   };
  } ;
  firstThread.start();
 }

 static
 {
//rate publisher thread, generates a new value for USD rate every 2 seconds.
  secondThread=new Thread(){
   public void run() {
    
    while(true)
    {
        if(queue!=null)
     {
         for (int i = 0; i < stock_quotes.length; i++) {
             if(stock_values[i] != temp_stock_values[i])
             {
                 sendAll(stock_quotes[i],Double.toString(stock_values[i]));
                 temp_stock_values[i] = stock_values[i];
             }
             
         }
     }
     try {
      sleep(2000);
     } catch (InterruptedException e) {      
     }
    }
   };
  } ;
  secondThread.start();
 }


    @OnOpen
        public void open(Session session) {
        queue.add(session);
          System.out.println("New session opened: "+session.getId());                   
            
    }

    @OnClose
        public void close(Session session) {
            queue.remove(session);
            System.out.println("session closed: "+session.getId());
    }

    @OnError
        public void onError(Session session, Throwable t) {
            queue.remove(session);
            System.err.println("Error on session "+session.getId());  
    }

    @OnMessage
        public void handleMessage(String message, Session session) throws EncodeException {
            System.out.println("subscribtion done");
            //queue = session.getOpenSessions();  
        stock_quotes1 = message.split(",");
        for (int i = 0; i < stock_quotes1.length; i++) {
            hm.put(session.getId(), stock_quotes1[i]);
            
        }
    
    }
        private static void sendAll(String msg,String msg1) {
            try {
                    ArrayList<Session > closedSessions= new ArrayList<>();
                    for (Session session : queue) {
                        if(!session.isOpen())
                        {
                            System.err.println("Closed session: "+session.getId());
                            closedSessions.add(session);
                        }
                        else
                        {
                            for (Map.Entry<String, String> entrySet : hm.entrySet()) {
                                 String key = entrySet.getKey();
                                String value = entrySet.getValue();
                                
                                q.put(value, msg1);
                                if(key.equals(session.getId()) && value.equals(msg))
                                {
                                    session.getBasicRemote().sendObject(q);
                                }
                            }
                            
                        }    
             }
             queue.removeAll(closedSessions);
             System.out.println("Sending "+msg+" to "+queue.size()+" clients");
                
            } catch (Throwable e) {
             e.printStackTrace();
            }
 }
}
