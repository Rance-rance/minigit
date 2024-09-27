
import com.minigit.MinigitApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.File;
import java.util.Properties;

@Slf4j
@SpringBootTest(classes = MinigitApplication.class)
public class UploadTest {
    @Test
    public void pathTest(){
        String path = "D:\\Learning Data\\minigit-test\\dir0\\dir1\\1.txt";
        System.out.println(File.separator);
        String path1 = path.replace(File.separator,"/");
        System.out.println(path1);
        String repoPath = "D:\\Learning Data\\minigit-test";
        String path2 = path.replace(repoPath + File.separator, "");
        System.out.println(path2);
    }

    @Test
    public void contentTest(){
        String content = "111111111111111111\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n";
        String[] lines = content.split(System.lineSeparator() + System.lineSeparator());
        for (String line : lines) {
            System.out.println(line);
        }
        String[] lines2 = content.split("\\\\r?\\\\n");
        for (String s : lines2) {
            System.out.println(s);
        }
    }


}
