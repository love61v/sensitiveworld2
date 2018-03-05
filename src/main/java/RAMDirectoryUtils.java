import org.apache.lucene.store.RAMDirectory;

/**
 * 内存目录单例
 * Date: 2018-03-03 下午11:11
 * Description:
 **/
public class RAMDirectoryUtils {

    private RAMDirectoryUtils() {
    }


    public static RAMDirectory getRAMDirectoryInstance() {
        return RAMDirectoryHolder.DIRECTORY;
    }

    static class RAMDirectoryHolder {
        private static RAMDirectory DIRECTORY = new RAMDirectory();
    }
}
