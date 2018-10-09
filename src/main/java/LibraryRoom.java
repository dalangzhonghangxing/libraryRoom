import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static java.lang.System.out;

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

    private static boolean isA;
    private static boolean isB;
    private static boolean isC;
    private static boolean isD;

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

        if (iswait) {
            try {

                while (waitTime > System.currentTimeMillis()) {
                    out.println("还剩" + (waitTime - System.currentTimeMillis()) / 1000 + "秒");
                    Thread.sleep(Math.min(30 * 1000, waitTime - System.currentTimeMillis()));
                    waitTime = new Date(today + " " + timeBegin).getTime() - aheadSecond * 1000;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void login() {
        head = new HashMap();
        gson.fromJson(
                HttpRequest.sendHttpGet(prefix + "/login.aspx?id=" + username + "&pwd=" + password + "&act=login", head),//用户名，密码
                Map.class);

        head.put("Cookie", "ASP.NET_SessionId=" + head.get("ASP.NET_SessionId"));
        head.remove("ASP.NET_SessionId");
        out.println("登陆成功，Cookie为" + head.get("Cookie").toString());
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
            out.println("第" + count + "次尝试，当前时间为" + new SimpleDateFormat("HH:mm:ss").format(new Date()));

            seat(Room429);
            seat(Room411);
            seat(Room415);

            seat(Room412);
            seat(Room414);
            seat(Room422);
            seat(Room424);
            seat(Room413);
            seat(Room423);
            seat(Room425);
            seat(Room426);
            seat(Room428);
            seat(Room427);

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
                out.println("抢到房间" + roomNumber + "，当前时间为" + new SimpleDateFormat("HH:mm:ss").format(new Date())
                        + "\t from " + start_time + " to " + end_time);
            } else {
                out.println(res.toString());
            }
        }
    }

    private static void seat(String roomNumber) {
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
        if (temp == 4)
            processOne(roomNumber,
                    "17%3A10", "21%3A00", "1710", "2100", "d");
    }

    public static void main(String[] args) {
        while (true) {
//         username = "51184407117";
//         password = "OTY_881227";
//            username = "51184407122";
//            password = "gushiyi_2126";
//         username = "51164500067";
//         password = "guhang123";
            username = args[0];
            password = args[1];
            out.println("用户名为:" + username);

            login();

            // 当天 21:00:30 之后，则睡眠等待下一天。
            today = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
            long stopTime = new Date(today + " " + "21:00:05").getTime();
            if (System.currentTimeMillis() > stopTime) {
                try {
                    out.println("已过当天截止时间，等待下一天");
                    Thread.sleep(4 * 3600 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isA = false;
            isB = false;
            isC = false;
            isD = false;

            init(true);
            process();
        }
    }

}
