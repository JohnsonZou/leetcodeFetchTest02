package com.johnson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
public class Test_Version01{
    public static void main(String[] args) throws IOException {

        System.setProperty("https.proxyHost", "127.0.0.1"); // set proxy server
        System.setProperty("https.proxyPort", "11000"); // set proxy port
        String contestName="biweekly-contest-90";
        int contestStartTime=1667053800;
        int contestEndTime=contestStartTime+60*90;
        System.out.println(contestStartTime);
        System.out.println(contestEndTime);
        System.out.println();

        String[] username=new String[30001];
        boolean[] datacn=new boolean[30001];
        double[] score=new double[30001];
        boolean[] attend=new boolean[30001];
        boolean[] mightAbsent=new boolean[30001];
        int[] contestHis=new int[30001];
        double[] newscore=new double[30001];
        String[]  pages=new String[1501];
        String[] titleSlug=new String[5];

        String html="https://leetcode.com/contest/api/ranking/"
                +contestName
                +"/?pagination=1&region=global";

        String s = Jsoup.connect(html)
                .ignoreContentType(true).timeout(50000)
                .get().body().toString();

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
        for(int i=0,lb=0,p=0;i<s.length();i++){
            if(s.charAt(i)=='['){
                lb++;
                if(lb==3)break;
            }
            if(lb<2)continue;
            if(s.startsWith("title_slug", i)){
                i+=13;
                StringBuilder tmp= new StringBuilder();
                while(s.charAt(i)!='"'){
                    tmp.append(s.charAt(i));
                    i++;
                }
                System.out.println(tmp.toString());
                titleSlug[++p]= tmp.toString();
            }
        }
        int totalPageNum=(contestantNum-1)/25+1;

        System.out.println("contest user page num:"+totalPageNum);
        System.out.println();

        int startPage=1;



        totalPageNum=startPage=720;


        for(int i=startPage;i<=totalPageNum;i++){
            pages[i]=Jsoup.connect(
                    "https://leetcode.com/contest/api/ranking/"
                    +contestName
                    +"/?pagination="
                    +String.valueOf(i)
                    +"&region=global")
                    .ignoreContentType(true).timeout(50000).get().body().toString();
        }
        int cnt=0;
        boolean zeroStart=false;
        for(int i=startPage;i<=totalPageNum;i++){
            String currentPageString=pages[i];
            int rightBracket=0;
            for(int j=0;j<currentPageString.length()-15;j++){
                if(currentPageString.charAt(j)==']')rightBracket++;
                if(rightBracket<2)continue;
                if(currentPageString.startsWith("user_slug", j)){
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
                if(currentPageString.startsWith("data_region", j)){
                    j+=14;
                    char c=currentPageString.charAt(j);
                    datacn[cnt]= c == 'C';
                }
                if(!zeroStart) {
                    if(currentPageString.startsWith("score", j)){
                        j+=7;
                        char c=currentPageString.charAt(j);
                        if(c=='0'){
                            zeroStart=true;
                        }
                    }
                }
                mightAbsent[cnt]=zeroStart;
            }
        }

        System.out.println("total contestant:"+cnt);
        System.out.println();

        for(int i=1;i<=cnt;i++){
            String curUser=username[i];
            String curPage;
            double sc=1500;
            int contestNum=0;
            if(datacn[i]){//data region in CN
                String bodyRest1="{\"query\":\"\\n query userContestRankingInfo($userSlug: String!) {\\n  userContestRanking(userSlug: $userSlug) {\\n     rating\\n    attendedContestsCount\\n  }\\n  \\n}\\n    \",\"variables\":{\"userSlug\":\"";
                String bodyRest2="\"}}";
                curPage = Jsoup.connect("https://ssg.leetcode.cn/graphql/noj-go/")
                        .ignoreContentType(true).timeout(50000)
                        .ignoreHttpErrors(true)
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .requestBody(bodyRest1+curUser+bodyRest2)
                        .post().body().toString();
            }
            else {//data region in US
                String bodyRest1 = "{\"query\":\"\\n    query userContestRankingInfo($username: String!) {\\n  userContestRanking(username: $username) {\\n rating\\n   attendedContestsCount\\n }\\n \\n}\\n\",\"variables\":{\"username\":\"";
                String bodyRest2 = "\"}}";
                curPage = Jsoup.connect("https://leetcode.com/graphql/")
                        .ignoreContentType(true).timeout(50000)
                        .ignoreHttpErrors(true)
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .requestBody(bodyRest1 + curUser + bodyRest2)
                        .post().body().toString();
            }
            if(false)attend[i]=true;
            else{
                attend[i]=false;
                if(datacn[i]){//data region in CN
                    String body1="{\"operationName\":\"RecentSubmissions\",\"variables\":{\"userSlug\":\"";
                    String body2="\"},\"query\":\"query RecentSubmissions($userSlug: String!) { recentSubmissions(userSlug: $userSlug) { status submitTime  question { titleSlug } } }\"}";
                    String recentSub = Jsoup.connect("https://leetcode.cn/graphql/")
                            .ignoreContentType(true).timeout(50000)
                            .ignoreHttpErrors(true)
                            .header("Content-Type", "application/json; charset=UTF-8")
                            .requestBody(body1+curUser+body2)
                            .post().body().toString();
                    int curTime=0;
                    for(int j=0;j<recentSub.length()-15;j++){
                        if(recentSub.startsWith("submitTime",j)){
                            j+=12;
                            curTime=0;
                            while(recentSub.charAt(j)!=','){
                                curTime=curTime*10+recentSub.charAt(j)-'0';
                                j++;
                            }
                        }
                        if(curTime>contestEndTime||curTime<contestStartTime)continue;
                        if(recentSub.startsWith("titleSlug",j)){
                            j+=12;
                            String curTitle="";
                            while(recentSub.charAt(j)!='\"'){
                                curTitle+=recentSub.charAt(j);
                                j++;
                            }
                            //System.out.println(curTime+" "+curTitle);
                            boolean att=false;
                            for(int k=1;k<=4;k++){
                                if(titleSlug[k].equals(curTitle)){
                                    attend[i]=att=true;
                                    break;
                                }
                            }
                            if(att)break;
                        }
                    }
                }
                else {//data region in US
                    String body1 = "{\"query\":\"\\n    query recentSubmissionList($username: String!) {\\n  recentSubmissionList(username: $username) {\\n   status\\n  titleSlug\\n    timestamp\\n  }\\n}\\n    \",\"variables\":{\"username\":\"";
                    String body2 = "\"}}";
                    String recentSub = Jsoup.connect("https://leetcode.com/graphql/")
                            .ignoreContentType(true).timeout(50000)
                            .ignoreHttpErrors(true)
                            .header("Content-Type", "application/json; charset=UTF-8")
                            .requestBody(body1 + curUser + body2)
                            .post().body().toString();
                    int curTime=0;
                    String curTitle="";
                    for(int j=0;j<recentSub.length()-15;j++){
                        if(recentSub.startsWith("titleSlug",j)){
                            j+=12;
                            curTitle="";
                            while(recentSub.charAt(j)!='\"'){
                                curTitle+=recentSub.charAt(j);
                                j++;
                            }
                        }
                        if(recentSub.startsWith("timestamp",j)){
                            j+=12;
                            curTime=0;
                            while(recentSub.charAt(j)!='\"'){
                                curTime=curTime*10+recentSub.charAt(j)-'0';
                                j++;
                            }
                            if(curTime>contestEndTime)continue;
                            if(curTime<contestStartTime)break;
                            System.out.println(curTime+" "+curTitle);
                            boolean att=false;
                            for(int k=1;k<=4;k++){
                                if(titleSlug[k].equals(curTitle)){
                                    attend[i]=att=true;
                                    break;
                                }
                            }
                            if(att)break;
                        }
                    }
                }
            }
            if(curPage.length()<53){
                contestHis[i]=0;
                score[i]=1500;
            }
            else{
                for(int j=20;j<curPage.length()-20;j++){
                    if(curPage.startsWith("attendedContestsCount", j)){
                        j+=23;
                        StringBuilder tmp= new StringBuilder();
                        char c=curPage.charAt(j);
                        while(c!='}'){
                            tmp.append(c);
                            j++;
                            c=curPage.charAt(j);
                        }
                        if(tmp.toString().isEmpty()){
                            contestHis[i]=0;
                        }
                        else{
                            contestHis[i]=Integer.parseInt(tmp.toString());
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
                            score[i]=Double.parseDouble(tmp.toString());
                        }
                    }
                }
            }
            System.out.println(i+"  "+username[i]+"    "+score[i]+" "+contestHis[i]+"   "+attend[i]);
        }
    }
}
