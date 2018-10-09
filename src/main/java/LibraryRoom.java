import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Hello world!
 */
public class LibraryRoom extends Thread {

    private int temp;
    private String roomNumber;


    public LibraryRoom(int temp, String roomNumber) {
        super();
        this.temp = temp;
        this.roomNumber = roomNumber;
    }

    private static boolean isA = false;
    private static boolean isB = false;
    private static boolean isC = false;
    private static boolean isD = false;

    private static String date = "";
    private static String today = "";
    private static int aheadSecond = 120; //提前aheadSecond秒开始慢速发请求
    private static String timeBegin = "20:58:45"; //�?始快速发请求时间
    private static int adheadDay = 2;
    private static long interval = 500;

    private static String username = "51184407122";
    private static String password = "gushiyi_2126";

    private static String prefix = "http://202.120.82.2:8081/ClientWeb/pro/ajax";

    private static Map head = new HashMap<String, String>();

    private static Gson gson = new GsonBuilder().create();


    private static void init(boolean iswait) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.add(Calendar.DATE, adheadDay);
        date = new SimpleDateFormat("yyyy-MM-dd").format(dateCalendar.getTime());
        today = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        long waitTime = new Date(today + " " + timeBegin).getTime() - aheadSecond * 1000;

        if (iswait)
            try {
                while (waitTime > System.currentTimeMillis()) {
                    System.out.println("还剩" + (waitTime - System.currentTimeMillis()) / 1000 + "秒");
                    Thread.sleep(Math.min(30 * 1000, waitTime - System.currentTimeMillis()));
                    waitTime = new Date(today + " " + timeBegin).getTime() - aheadSecond * 1000;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    private static void login() {
        head = new HashMap();
        gson.fromJson(
                HttpRequest.sendHttpGet(prefix + "/login.aspx?id=" + username + "&pwd=" + password + "&act=login", head),//用户名，密码
                Map.class);

        head.put("Cookie", "ASP.NET_SessionId=" + head.get("ASP.NET_SessionId"));
        head.remove("ASP.NET_SessionId");
        System.out.println("登陆成功，Cookie为" + head.get("Cookie").toString());
    }

    @SuppressWarnings("rawtypes")
    private static void process() {

        login();

        //玻璃门
        final String Room411 = "3676604";
        final String Room412 = "3676641";
        final String Room413 = "3676645";
        final String Room414 = "3676656";
        final String Room415 = "3676664";
        // 木门
//		final String Room421 = "3676503";
        final String Room422 = "3676511";
        final String Room423 = "3676515";
        final String Room424 = "3676522";
        final String Room425 = "3676538";
        final String Room426 = "3676547";
        final String Room427 = "3676566";
        final String Room428 = "3676574";
        final String Room429 = "3676580";

        int count = 1;
        while (!isA || !isB || !isC || !isD) {
            System.out.println("第" + count + "次尝试，当前时间为" + new SimpleDateFormat("HH:mm:ss").format(new Date()));

            seat(Room429, gson, prefix, head);
//            seat(Room411, gson, prefix, head);
//            seat(Room415, gson, prefix, head);
//            seat(Room412, gson, prefix, head);
//			seat(Room413, gson, prefix, head);
//			seat(Room423, gson, prefix, head);
//			seat(Room425, gson, prefix, head);
//			seat(Room426, gson, prefix, head);
//			seat(Room428, gson, prefix, head);
//			seat(Room424, gson, prefix, head);

            try {
                // 间隔1秒，以防玩崩
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }

    }

    private void processOne(String roomNumber, String start, String end, String start_time, String end_time, String type) {
        boolean flag;
        switch (type) {
            case "a":
                flag = isA;
                break;
            case "b":
                flag = isB;
                break;
            case "c":
                flag = isC;
                break;
            case "d":
                flag = isD;
                break;
            default:
                flag = isA;
        }
        if (!flag) {
            String res = gson
                    .fromJson(
                            HttpRequest.sendHttpGet(prefix + "/reserve.aspx?dev_id=" + roomNumber
                                    + "&lab_id=3674920&kind_id=3674969&type=dev&prop=&test_id=&term=&test_name=&"
                                    + "start=" + date + "+" + start + "&end=" + date + "+" + end + "&start_time=" + start_time
                                    + "&end_time=" + end_time + "&up_file=&memo=&act=set_resv&_=1497964540852%20", head),
                            Map.class)
                    .get("msg").toString();
            flag = res.contains("成功");
            if (flag) {
                switch (type) {
                    case "a":
                        isA = true;
                        break;
                    case "b":
                        isB = true;
                        break;
                    case "c":
                        isC = true;
                        break;
                    case "d":
                        isD = true;
                        break;
                    default:
                        break;
                }
                System.out.println("抢到房间" + roomNumber + "，当前时间为" + new SimpleDateFormat("HH:mm:ss").format(new Date())
                        + "\t from " + start_time + " to " + end_time);
            } else {
                System.out.println(res.toString());
            }
        }
    }

    @Override
    public void run() {
        if (temp == 1)
            processOne(roomNumber,
                    "8%3A00", "10%3A40", "830", "1040", "a");
        if (temp == 2)
            processOne(roomNumber,
                    "10%3A50", "12%3A50", "1050", "1250", "b");
        if (temp == 3)
            processOne(roomNumber,
                    "13%3A00", "17%3A00", "1300", "1700", "c");
//            processOne(roomNumber,
//                "17%3A40", "18%3A30", "1740", "1830", "c");
        if (temp == 4)
            processOne(roomNumber,
                    "17%3A10", "21%3A00", "1710", "2100", "d");
    }

    private static void seat(String roomNumber, Gson gson, String prefix, Map head) {
        if (!isA || !isB || !isC || !isD) {
            LibraryRoom threadA = new LibraryRoom(1, roomNumber);
            threadA.run();
            LibraryRoom threadB = new LibraryRoom(2, roomNumber);
            threadB.run();
            LibraryRoom threadC = new LibraryRoom(3, roomNumber);
            threadC.run();
            LibraryRoom threadD = new LibraryRoom(4, roomNumber);
            threadD.run();

            // 串行化处理
            try {
                threadA.join();
                threadB.join();
                threadC.join();
                threadD.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
//        login();
//         username = "51184407117";
//         password = "OTY_881227";
//         username = "51184407122";
//         password = "gushiyi_2126";
         username = "51164500067";
         password = "guhang123";
        init(true);
        process();
    }

}
