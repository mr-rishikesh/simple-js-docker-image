import java.util.*;

public class A {

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    

    int test = sc.nextInt();

    for (int i = 0; i < test; i++) {
      int n = sc.nextInt();
      int ans = 0;

      while(n > 2) {

        int val = n / 3 + n% 3;
        ans +=  n / 3;
        n = val;

      }

      System.out.println(ans);

    }
  }
}