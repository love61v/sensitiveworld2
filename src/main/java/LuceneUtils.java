import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
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
    private static final IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_43, new IKAnalyzer());

    /**
     * 系统启动后就生成索引到内存
     */
    public static void createIndex(String content) {
        try {
            IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
            Document doc = new Document();
            doc.add(new Field(FIELD_NAME, content, TextField.TYPE_STORED));

            indexWriter.addDocument(doc);
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateIndex(String content) {
        try {
            IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
            Document doc = new Document();
            doc.add(new Field(FIELD_NAME, content, TextField.TYPE_STORED));
            indexWriter.deleteAll();
            indexWriter.commit();
            indexWriter.updateDocument(new Term(FIELD_NAME), doc);
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearAll() {
        try {
            IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
            indexWriter.deleteAll();
            indexWriter.commit();
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
        String resultTxt = queryTxt;
        System.out.println(String.format("查询%s条关键词", wordsLib.length));
        try {
            for (String word : wordsLib) {
                DirectoryReader directoryReader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(directoryReader);
                IKAnalyzer analyzer = new IKAnalyzer();

                QueryParser parser = new QueryParser(Version.LUCENE_43, FIELD_NAME, analyzer);
                parser.setDefaultOperator(QueryParser.Operator.AND);
                Query query = parser.parse(word);
                TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
                long totalHits = topDocs.totalHits;
                if (totalHits == 0) {
                    continue;
                }
                Highlighter highlighter = createHighlighter(query);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    String str = doc.get(FIELD_NAME);

                    // 高亮
                    Optional<String> result = hightlight(highlighter, analyzer, FIELD_NAME, str);
                    resultTxt = replaceSensitiveWord(result.get());
                    updateIndex(resultTxt);
                }
            }
            list.add(resultTxt);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            clearAll();
        }

        return list;
    }

    private static String replaceSensitiveWord(String str) {
        return Optional.ofNullable(str).orElse("").replaceAll("\\{.*?\\}", "*");
    }


    private static Highlighter createHighlighter(Query query) {
        final Formatter formatter = new SimpleHTMLFormatter("{", "}");
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
            result = highlighter.getBestFragment(analyzer.tokenStream(fieldName, new StringReader(plain)), plain);
        } catch (IOException | InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(result == null ? plain : result);
    }
}
