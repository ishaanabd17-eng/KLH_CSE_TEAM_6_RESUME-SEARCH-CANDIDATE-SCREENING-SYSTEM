import java.util.*;

public final class StringAlgorithms {
    private StringAlgorithms() {}

    public static boolean kmpContains(String text, String pattern) {
        text = TextUtils.normalize(text); pattern = TextUtils.normalize(pattern);
        if (pattern.isEmpty()) return true;
        int[] lps = new int[pattern.length()];
        for (int i=1, len=0; i<pattern.length();) {
            if (pattern.charAt(i)==pattern.charAt(len)) lps[i++]=++len;
            else if (len>0) len=lps[len-1]; else lps[i++]=0;
        }
        for (int i=0,j=0; i<text.length();) {
            if (text.charAt(i)==pattern.charAt(j)) {
                i++; j++; if (j==pattern.length()) return true;
            } else if (j>0) j=lps[j-1]; else i++;
        }
        return false;
    }

    public static int editDistance(String a, String b) {
        a=TextUtils.normalize(a); b=TextUtils.normalize(b);
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i=0;i<=a.length();i++) dp[i][0]=i;
        for (int j=0;j<=b.length();j++) dp[0][j]=j;
        for (int i=1;i<=a.length();i++) for (int j=1;j<=b.length();j++) {
            int cost = a.charAt(i-1)==b.charAt(j-1) ? 0 : 1;
            dp[i][j]=Math.min(Math.min(dp[i-1][j]+1,dp[i][j-1]+1),dp[i-1][j-1]+cost);
        }
        return dp[a.length()][b.length()];
    }
}
