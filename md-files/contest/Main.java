import java.util.*;
public class Main {
  

    public static void main(String[] args) {
       Scanner sc = new Scanner(System.in);
       

        int test = 0;

        test = sc.nextInt();


        for(int i =0;i < test;i++) {

          int n = sc.nextInt();
          int[] arr = new int[n];

          for(int j = 0;j<n;j++) {
            arr[j]  = sc.nextInt();
          }

          int x = sc.nextInt();

          int min = Math.min(arr[0] , arr[n-1]);
          int max = Math.max(arr[0] , arr[n-1]);

          if(min <= x && max >= x) {
            System.out.println("YES");
          }
          else {
            System.out.println("NO");
          }
               
  

      }

 
         
       

    }
}
