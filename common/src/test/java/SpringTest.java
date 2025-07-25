import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(classes = CloudDemoApplication.class)
public class SpringTest {

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> res = new ArrayList<>();
        int m = matrix.length;
        int n = matrix[0].length;
        int count = m * n;
        int startx = 0;
        int starty = 0;

        while(count > 0){
            int row = startx;
            int col = starty;

            // 上边向右
            for(; col < n - starty && count > 0; col++){
                res.add(matrix[row][col]);
                count--;
            }
            col--; // 回退到边界
            row++; // 下移到下一行

            // 右边向下
            for(; row < m - startx && count > 0; row++){
                res.add(matrix[row][col]);
                count--;
            }
            row--; // 回退到边界
            col--; // 左移

            // 下边向左
            for(; col >= starty && count > 0; col--){
                res.add(matrix[row][col]);
                count--;
            }
            col++; // 回退到边界
            row--; // 上移

            // 左边向上
            for(; row > startx && count > 0; row--){
                res.add(matrix[row][col]);
                count--;
            }

            startx++;
            starty++;
        }

        return res;
    }

    @Test
    public void test(){

        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        List<Integer> result = spiralOrder(matrix);
        System.out.println(result); // 输出: [1, 2, 3, 6, 9, 8, 7, 4, 5]


    }
}
