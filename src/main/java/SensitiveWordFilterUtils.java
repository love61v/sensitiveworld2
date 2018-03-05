import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Date: 2018-03-04 上午12:51
 * Description:
 **/
public class SensitiveWordFilterUtils {
    //(代驾|整车)&(快递|保险)&(服务|提供)
    private static String[] words = {
            "(代驾 整车) AND (快递 保险) AND (服务 提供)",
            "(指*定) AND (账*户) AND (帮*我) AND (交*学*费)",
            "(顶l级 OR  家教 OR  内舍) AND (说了算 OR  行情 OR  身心) AND (唯信 OR  微信 OR  嶶5r信)",
            "(開 OR  资格) AND (票 OR  钢印) AND (13 OR  电)",
            "(欠歀 OR  埖無缼 OR  还钱 OR  一笔钱 OR  欠款 OR  全面公开 OR  还款) AND (通讯录 OR  连累 OR  性病 OR  专业追收 OR  死定 OR  亲戚 OR  亲友) AND (通知 OR  处理 OR  讨债 OR  做狗 OR  借款)",
            "(贷款) AND (绑卡 OR  办理) AND (申请 OR  借钱 OR  成功)",
            "(孩子) AND (体检报告) AND (评价)",
            "(代驾 OR  整车) AND (快递 OR  保险) AND (服务 OR  提供)",
            "(会员 OR  用户) AND (订单 OR  调查 OR  评价 OR  付费) AND (参加 OR  优质 OR  获得 OR  发布)",
            "(投递 OR  人脉) AND (好友 OR  岗位 OR  月薪) AND (查收 OR  推荐 OR  面议 OR  房补)",
            "(投递) AND (工作) AND (职位 OR  月薪)",
            "(真票 OR  服务 OR  真发 OR  曾拾) AND (机打 OR  需电 OR  坻扣 OR  代开)",
            "(信一誉 OR  信誉 OR  记录) AND (良好 OR  特好) AND (工作 OR  酬薪 OR  聘请)",
            "(一肖) AND (群内) AND (荍费 OR  无费)",
            "(年级 OR  培优 OR  上课) AND (补习 OR  老师 OR  晚托) AND (满分 OR  费用 OR  解析 OR  作业 OR  开设)",
            "(评估 OR  审核) AND (额度 OR  初审 OR  线上) AND (申请 OR  收款 OR  通过)",
            "(龙门娱乐) AND (在线 OR  百家乐) AND (送)",
            "(永利 OR  皇宫) AND (免费 OR  诚邀) AND (奖赏 OR  获)"
    };

    public static String doFilter(String plainText, String... wordsLib) {

        List<String> resultList = LuceneUtils.getHightlightStr(plainText, wordsLib);
        System.out.println("匹配关键字高亮结果: " + JSON.toJSONString(resultList));

        return Optional.ofNullable(resultList)
                       .orElse(new ArrayList<>()).stream()
                       .findFirst()
                       .map(str -> replaceSensitiveWord(str))
                       .orElse(plainText);
    }

    private static String replaceSensitiveWord(String str) {
        return Optional.ofNullable(str).orElse("").replaceAll("<b>.*?</b>", "*");
    }

    public static void main(String[] args) {
        String content = "我爱北京,代驾服务特别好,当然快v递也快，都是整车的送到家,真是提供了便利啊";
        long now = System.currentTimeMillis();
        System.out.println(String.format("当时时间: %s, 过滤前: %s", now, content));
        String result = doFilter(content, words);

        long time = (System.currentTimeMillis() - now);
        System.out.println(String.format("用时:%s , 过滤后为", time / 1000));
        System.out.println(result);
    }
}
