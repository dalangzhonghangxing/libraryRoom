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

    //玻璃门
    final static String Room411 = "3676604";
    final static String Room412 = "3676641";
    final static String Room413 = "3676645";
    final static String Room414 = "3676656";
    final static String Room415 = "3676664";
    // 木门
    final static String Room421 = "3676503";
    final static String Room422 = "3676511";
    final static String Room423 = "3676515";
    final static String Room424 = "3676522";
    final static String Room425 = "3676538";
    final static String Room426 = "3676547";
    final static String Room427 = "3676566";
    final static String Room428 = "3676574";
    final static String Room429 = "3676580";

    private static boolean isA;
    private static boolean isB;
    private static boolean isC;
    private static boolean isD;

    private static String date = "";
    private static String today = "";
    private static int aheadSecond = 30; //提前aheadSecond秒开始慢速发请求
    private static String timeBegin = "20:59:45"; //�?始快速发请求时间
    private static int adheadDay = 2;
    private static long interval = 1000;
    private static String room = null;
    private static boolean failed = false;

    private static String username = "51184407122";
    private static String password = "gushiyi_2126";

    private static String prefix = "http://202.120.82.2:8081/ClientWeb/pro/ajax";

    private static Map head = new HashMap<String, String>();

    private static Gson gson = new GsonBuilder().create();

    // 初始化
    private static void init(boolean iswait) {
        failed = false;
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.add(Calendar.DATE, adheadDay);
        date = new SimpleDateFormat("yyyy-MM-dd").format(dateCalendar.getTime());
        today = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        long waitTime = new Date(today + " " + timeBegin).getTime() - aheadSecond * 1000;

        if (iswait) {
            try {

                while (waitTime > System.currentTimeMillis()) {
                    out.println("还剩" + (waitTime - System.currentTimeMillis()) / 1000 + "秒");
                    Thread.sleep(Math.min(600 * 1000, waitTime - System.currentTimeMillis()));
                    waitTime = new Date(today + " " + timeBegin).getTime() - aheadSecond * 1000;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 登陆
    private static void login() {
        head = new HashMap();
        gson.fromJson(
                HttpRequest.sendHttpGet(prefix + "/login.aspx?id=" + username + "&pwd=" + password + "&act=login", head),//用户名，密码
                Map.class);

        head.put("Cookie", "ASP.NET_SessionId=" + head.get("ASP.NET_SessionId"));
        head.remove("ASP.NET_SessionId");
        out.println("登陆成功，Cookie为" + head.get("Cookie").toString());
    }

    /**
     * 开始抢
     */
    private static void process() {

        login();

        int count = 1;
        while (!isA || !isB || !isC || !isD) {
            out.println("第" + count + "次尝试，当前时间为" + new SimpleDateFormat("HH:mm:ss").format(new Date()));

            seat(room);

            if (failed) {
                // 如果失败，则同时开启多个线程，进行轰炸式抢占
                System.out.println("失败了，开始轰炸模式");
                seat(Room413);
                seat(Room414);
                seat(Room422);
                seat(Room423);
                seat(Room412);
                break;
            }

            try {
                // 间隔一段时间，以防玩崩
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;

            // 超过9点，则不抢了
            long stopTime = new Date(today + " " + "21:00:08").getTime();
            if (System.currentTimeMillis() > stopTime) {
                System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "没有全完抢到，已过截止时间");
                break;
            }
        }
        if (isA && isB && isC && isD) {
            System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "全部抢到");

        }

    }

    /**
     * 抢一个房间，一个时段的位置
     */
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

            if (res.contains("预约与现有预约冲突")) {
                // 被别人抢掉，启用候补策略
                failed = true;
            }

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

    /**
     * 抢座位，同时开四个线程
     *
     * @param roomNumber
     */
    private static void seat(String roomNumber) {
        if (!isA || !isB || !isC || !isD) {
            LibraryRoom threadA = new LibraryRoom(1, roomNumber);
            threadA.start();
            LibraryRoom threadB = new LibraryRoom(2, roomNumber);
            threadB.start();
            LibraryRoom threadC = new LibraryRoom(3, roomNumber);
            threadC.start();
            LibraryRoom threadD = new LibraryRoom(4, roomNumber);
            threadD.start();

            // 串行化处理
            try {
//                threadA.join();
//                threadB.join();
//                threadC.join();
//                threadD.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {
        // 设置时间。
        // %3A是转义字符，每两个时间段之间要间隔10分钟，不然会抢占失败.
        // 一个时间段最长4小时
        if (temp == 1)//9:00-12:20
            processOne(roomNumber,
                    "9%3A00", "12%3A00", "900", "1200", "a");
        if (temp == 2)//12:30-16:30
            processOne(roomNumber,
                    "12%3A10", "16%3A10", "1210", "1610", "b");
        if (temp == 3)//16:40-20:40
            processOne(roomNumber,
                    "16%3A10", "20%3A10", "1610", "2010", "c");
        if (temp == 4)//20:50-21:50
            processOne(roomNumber,
                    "20%3A20", "21%3A50", "2020", "2150", "d");
    }

    /**
     * 设置策略，一天总共分4个时间段，true表示不抢，false表示抢占。
     * 时间段在run方法中设置。
     * day从周日开始算，即day=0是周日，1是周一，7是周六。
     */
    private static void setStrategy() {
        int days[] = {7, 1, 2, 3, 4, 5, 6};
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.add(Calendar.DATE, 2);
        int day = dateCalendar.get(Calendar.DAY_OF_WEEK);
        System.out.println("要抢占的是周 " + days[day - 1] + " 的房间");
        isA = true;
        isB = true;
        isC = true;
        isD = true;

        if (day == 1 || day == 7) { //周六周日
            isA = false;
            isB = false;
            isC = false;
        }

    }

    // 超过21:00:05，等到下一天
    private static void waitToNextDay() {
        today = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        long stopTime = new Date(today + " " + "21:00:10").getTime();
        if (System.currentTimeMillis() > stopTime) {
            try {
                out.println("已过当天截止时间，等待下一天");
                Thread.sleep(20 * 3600 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置房间号。下面是所有房间号列表,41x是玻璃房，42x是木门的
     *
     * @param roomNumber
     */
    private static void setRoom(int roomNumber) {
        switch (roomNumber) {
            case 411:
                room = Room411;
                break;
            case 412:
                room = Room412;
                break;
            case 413:
                room = Room413;
                break;
            case 414:
                room = Room414;
                break;
            case 415:
                room = Room415;
                break;
            case 421:
                room = Room421;
                break;
            case 422:
                room = Room422;
                break;
            case 423:
                room = Room423;
                break;
            case 424:
                room = Room424;
                break;
            case 425:
                room = Room425;
                break;
            case 426:
                room = Room426;
                break;
            case 427:
                room = Room427;
                break;
            case 428:
                room = Room428;
                break;
            case 429:
                room = Room429;
                break;
            default:
                room = Room429;
        }
    }

    /**
     * 每天9点抢后天的图书馆，设置的时间是要抢的那一天，比如周一抢周三的，设置的策略是周三的
     * 进去target目录，执行 java -jar LibraryRoom.jar 学号 密码 房间号
     * 只需要修改setStrategy、run这两个方法，运行之后可不关闭，会再后台一直抢占，不同每天运行
     * 51184407122 gushiyi_2126
     */
    public static void main(String[] args) {
        while (true) {
            username = args[0];
            password = args[1];
            int roomNumber = Integer.parseInt(args[2]);

            out.println("用户名为:" + username);

            // 设置要抢的房间号
            setRoom(roomNumber);

            out.println("要抢占的房间号为:" + roomNumber);

            login();

            // 设置策略
            setStrategy();

            // 当天 21:00:30 之后，则睡眠等待下一天。
            waitToNextDay();

            init(true);
            process();

            // 抢完后再等待下一天
            try {
                out.println("已过当天截止时间，等待下一天");
                Thread.sleep(23 * 3600 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
