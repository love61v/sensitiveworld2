import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: hubo11@meituan.com
 * Date: 2018-03-04 上午12:51
 * Description:
 **/
public class SensitiveWordFilterUtils {
    //(代驾|整车)&(快递|保险)&(服务|提供)
    private static String[] words = {
            "(代驾 OR 整车) AND (快递 OR 保险) AND (服务 OR 提供)"
    };

    public static String doFilter(String plainText, String... wordsLib) {
        List<String> resultList = LuceneUtils.getHightlightStr(plainText, wordsLib);
        System.out.println("匹配关键字高亮结果: " + JSON.toJSONString(resultList));

        return Optional.ofNullable(resultList)
                       .orElse(new ArrayList<>()).stream()
                       .findFirst().map(str -> { return str.replaceAll("<b>.*?</b>", "*"); })
                       .orElse(plainText);
    }

    public static void main(String[] args) {
        String content = "我爱北京,代驾服务特别好,当然快递也快，都是整车的送到家,真是提v供了便利啊";
        System.out.println("过滤前: " + content);
        String result = doFilter(content, words);

        System.out.println("过滤后为");
        System.out.println(result);
    }
}
