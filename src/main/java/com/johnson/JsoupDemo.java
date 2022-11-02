package com.johnson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupDemo {
    public static void main(String[] args) throws IOException {

        System.setProperty("https.proxyHost", "127.0.0.1"); // set proxy server
        System.setProperty("https.proxyPort", "11000"); // set proxy port
        String contestName="weekly-contest-307";
        String contestCNName="第 307 场周赛";
        String contestBeforeCN="第 85 场双周赛";
        String contestBefore="Biweekly Contest 85";

        //https://leetcode.com/contest/api/ranking/weekly-contest-307/?pagination=3&region=global


        String html="https://leetcode.com/contest/api/ranking/"+contestName+"/?pagination=1&region=global";

        Document doc = Jsoup.connect(html)
                .ignoreContentType(true)
                .get();
        String s=doc.body().toString();
        int contestantNum=0;
        for(int i=s.length()-1;;i--){
            if(s.charAt(i)=='}'){
                i--;
                int j=i;
                while(s.charAt(i)>='0'&&s.charAt(i)<='9'){
                    i--;
                }
                for(int k=i+1;k<=j;k++){
                    contestantNum=contestantNum*10+s.charAt(k)-'0';
                }
                break;
            }
        }
        int totalPageNum=(contestantNum-1)/25+1;

        System.out.println("contest user page num:"+totalPageNum);
        System.out.println();

        String[] username=new String[30001];
        boolean[] datacn=new boolean[30001];
        double[] score=new double[30001];
        boolean[] attend=new boolean[30001];
        int[] contestHis=new int[30001];
        double[] newscore=new double[30001];
        String[]  pages=new String[1501];

        int startPage=1;
        totalPageNum=1;
        for(int i=startPage;i<=totalPageNum;i++){
            pages[i]=Jsoup.connect("https://leetcode.com/contest/api/ranking/"+contestName+"/?pagination="+String.valueOf(i)+"&region=global")
                    .ignoreContentType(true).get().body().toString();
        }
        int cnt=0;
        for(int i=startPage;i<=totalPageNum;i++){
            String currentPageString=pages[i];
            boolean stascanf=false;
            int rightBracket=0;
            for(int j=0;j<currentPageString.length()-15;j++){
                if(currentPageString.charAt(j)==']')rightBracket++;
                if(rightBracket<2)continue;
                if(currentPageString.startsWith("user_slug", j)){
                    stascanf=true;
                    StringBuilder tmp= new StringBuilder();
                    j+=12;
                    char c=currentPageString.charAt(j);
                    while(c!='"'){
                        tmp.append(c);
                        j++;
                        c=currentPageString.charAt(j);
                    }
                    username[++cnt]= tmp.toString();
                }
                if(stascanf){
                    if(currentPageString.startsWith("data_region", j)){
                        j+=14;
                        char c=currentPageString.charAt(j);
                        datacn[cnt]= c == 'C';
                    }
                }
            }
        }

        System.out.println("total contestant:"+cnt);
        System.out.println();

        for(int i=1;i<=cnt;i++){
            String curUser=username[i];
            String curPage;
            double sc=1500;
            double cursc=1500;
            int scan=0;
            int contestNum=0;
            boolean att=false;
            if(datacn[i]){//data region in CN
                String bodyRest1="{\"query\":\"\\nquery userContestRankingInfo($userSlug: String!) {\\nuserContestRankingHistory(userSlug:$userSlug) {\\nattended\\nrating\\ncontest {\\ntitle\\n}\\n}\\n}\\n\",\"variables\":{\"userSlug\":\"";
                String bodyRest2="\"}}";
                curPage = Jsoup.connect("https://ssg.leetcode.cn/graphql/noj-go/")
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .requestBody(bodyRest1+curUser+bodyRest2)
                        .post().body().toString();
            }
            else {//data region in US
                String bodyRest1 = "{\"query\":\"\\n    query userContestRankingInfo($username: String!) {\\n   userContestRankingHistory(username: $username) {\\nattended\\nrating\\ncontest {\\n      title\\n      }\\n  }\\n}\\n    \",\"variables\":{\"username\":\"";
                String bodyRest2 = "\"}}";
                curPage = Jsoup.connect("https://leetcode.com/graphql/")
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .requestBody(bodyRest1 + curUser + bodyRest2)
                        .post().body().toString();
            }
            for(int j=10;j<curPage.length()-20;j++){
                if(scan==1){
                    if(curPage.startsWith("attended", j)){
                        j+=10;
                        if(curPage.charAt(j)=='t'){
                            att=true;
                        }
                    }
                    if(curPage.startsWith("rating", j)){
                        j+=8;
                        StringBuilder tmp= new StringBuilder();
                        char c=curPage.charAt(j);
                        while(c!=','){
                            tmp.append(c);
                            j++;
                            c=curPage.charAt(j);
                        }
                        if(Double.parseDouble(tmp.toString())!=0){
                            cursc=Double.parseDouble(tmp.toString());
                        }
                        break;
                    }
                }
                else{
                    if(curPage.startsWith("attended", j)){
                        j+=10;
                        if(curPage.charAt(j)=='t'){
                            contestNum++;
                        }
                    }
                    if(curPage.startsWith("rating", j)){
                        j+=8;
                        StringBuilder tmp= new StringBuilder();
                        char c=curPage.charAt(j);
                        while(c!=','){
                            tmp.append(c);
                            j++;
                            c=curPage.charAt(j);
                        }
                        if(Double.parseDouble(tmp.toString())!=0){
                            sc=Double.parseDouble(tmp.toString());
                        }
                    }
                    if(datacn[i]){
                        if(curPage.substring(j,j+9).equals(contestBeforeCN)){
                            scan=1;
                        }
                    }
                    else{
                        if(curPage.substring(j,j+19).equals(contestBefore)){
                            scan=1;
                        }
                    }
                }
            }
            score[i]=sc;
            attend[i]=att;
            newscore[i]=cursc;
            contestHis[i]=contestNum;
            System.out.println(sc+" "+cursc+" "+contestNum+" "+att);
        }
    }
}
