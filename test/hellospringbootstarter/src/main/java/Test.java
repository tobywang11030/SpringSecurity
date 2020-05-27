import java.util.Arrays;

/**
 * @author tobywang
 * @date 10/25/2019
 */
public class Test {
  
    
    public static void main(String[] args) {
        t1();
        System.out.println("<br>");
        t2();
    }
    
    public static void t1() {
        int[]  as = {17, 22, 62, 89, 63, 54, 62, 74};
        int r = 0;
        for (int i = 0; i< as.length ; i++) {
            for (int j = 0; j < as.length; j++) {
                if (as[i] == as [j] && i != j) {
                    r = as[i];
                }
            }
        }
        System.out.println(r);
    }
    
    public static void t2() {
        int[]  as = {17, 22, 62, 89, 63, 54, 62, 74};
        Arrays.sort(as);
        for (int a : as) {
            //System.out.println(a);
        }
        
        for (int i = 0; i < as.length -1; i++) {
            
            if (as[i] == as [i +1]) {
                System.out.println("R:");
                System.out.println(as[i]);
            }
        }
    }
    
   
    
}
