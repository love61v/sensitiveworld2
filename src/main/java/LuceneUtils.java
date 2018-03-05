import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Date: 2018-03-03 下午10:42
 * Description:
 **/
public class LuceneUtils {

    public static final String FIELD_NAME = "sensitiveWord";
    private static final RAMDirectory directory = RAMDirectoryUtils.getRAMDirectoryInstance();

    /**
     * 系统启动后就生成索引到内存
     */
    public static void createIndex(String content) {
        try {
            IndexWriterConfig writerConfig = new IndexWriterConfig(new SmartChineseAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
            Document doc = new Document();
            doc.add(new Field(FIELD_NAME, content, TextField.TYPE_STORED));

            indexWriter.addDocument(doc);
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取高亮关键词后的结果
     *
     * @param plainTxt
     * @param wordsLib
     * @return
     */
    public static List<String> getHightlightStr(String plainTxt, String... wordsLib) {
        createIndex(plainTxt);
        return search(plainTxt, wordsLib);
    }

    public static List<String> search(String queryTxt, String... wordsLib) {
        List<String> list = new ArrayList<>();

        try {
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(directoryReader);
            SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

            for (String word : wordsLib) {

                QueryParser parser = new ComplexPhraseQueryParser(FIELD_NAME, analyzer);
                Query query = null;
                try {
                    query = parser.parse("+(代驾 整车) AND (快递 保险) AND (服务 提供)");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Highlighter highlighter = createHighlighter(query);

                TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
                System.out.println("命中数量: " + topDocs.totalHits);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    String str = doc.get(FIELD_NAME);

                    // 高亮
                    Optional<String> result = hightlight(highlighter, analyzer, FIELD_NAME, str);
                    list.add(result.orElse(null));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    private static Highlighter createHighlighter(Query query) {
        final Formatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        QueryScorer queryScorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, queryScorer);
        highlighter.setFragmentScorer(queryScorer);

        return highlighter;
    }

    /**
     * 高亮
     *
     * @param highlighter
     * @param analyzer
     * @param fieldName
     * @param plain
     * @return
     */
    private static Optional<String> hightlight(Highlighter highlighter, Analyzer analyzer, String fieldName, String plain) {
        String result = null;

        try {
            result = highlighter.getBestFragment(analyzer.tokenStream(fieldName, plain), plain);
        } catch (IOException | InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(result == null ? plain : result);
    }
}
