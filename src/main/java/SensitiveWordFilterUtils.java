import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Date: 2018-03-04 上午12:51
 * Description:
 **/
public class SensitiveWordFilterUtils {

    private static String[] words = {
            "(代驾  整车) AND (快递  保险) AND (服务  提供)",
           "(指*定) AND (账*户) AND (帮*我) AND (交*学*费)",
            "(顶l级   家教   内舍) AND (说了算   行情   身心) AND (唯信   微信   嶶5r信)",
            "(開   资格) AND (票   钢印) AND (13   电)",
            "(欠歀   埖無缼   还钱   一笔钱   欠款   全面公开   还款) AND (通讯录   连累   性病   专业追收   死定   亲戚   亲友) AND (通知   处理   讨债   做狗   借款)",
            "(贷款) AND (绑卡   办理) AND (申请   借钱   成功)",
            "(孩子) AND (体检报告) AND (评价)",
            "(代驾   整车) AND (快递   保险) AND (服务   提供)",
            "(会员   用户) AND (订单   调查   评价   付费) AND (参加   优质   获得   发布)",
            "(投递   人脉) AND (好友   岗位   月薪) AND (查收   推荐   面议   房补)",
            "(投递) AND (工作) AND (职位   月薪)",
            "(真票   服务   真发   曾拾) AND (机打   需电   坻扣   代开)",
            "(信一誉   信誉   记录) AND (良好   特好) AND (工作   酬薪   聘请)",
            "(一肖) AND (群内) AND (荍费   无费)",
            "(年级   培优   上课) AND (补习   老师   晚托) AND (满分   费用   解析   作业   开设)",
            "(评估   审核) AND (额度   初审   线上) AND (申请   收款   通过)",
            "(龙门娱乐) AND (在线   百家乐) AND (送)",
            "(永利   皇宫) AND (免费   诚邀) AND (奖赏   获)"
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
        test(content);
        test("我爱北京,代驾服务特别好,当然快递也快，都是整车的送到家,真是提供了便利啊");
    }

    private static void test(String content) {
        long now = System.currentTimeMillis();
        System.out.println(String.format("过滤前: %s", content));
        String result = doFilter(content, words);

        long time = (System.currentTimeMillis() - now);
        System.out.println(String.format("过滤后为", time / 1000));
        System.out.println(result);
    }
}
