
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
public class GrammaticalAnalysis {

    /**
     * 文法G[E]
     * E→TE'
     * E'→+TE'|ε
     * T→FT'
     * T'→*FT'|ε
     * F→(E)|i
     */

    //预测分析表
    public String[][] analysistable= {
            {"TE'","","","TE'","",""},
            {"","+TE'","","","ε","ε"},
            {"FT'","","","FT'","",""},
            {"","ε","*FT'","","ε","ε"},
            {"i","","","(E)","",""}

    };
    public String[] VN= {"E","E'","T","T'","F"};
    public Character[] VT= {'i','+','*','(',')','#'};
    //非终结符集合的字符串
    private List<String> VNlist=Arrays.asList(VN);
    //终结符集合的字符串
    private List<Character> VTlist=Arrays.asList(VT);
    //分析栈
    public Stack<String> stack=new Stack<>();

    //分析表达式的全部过程
    public void process(String str) {

        System.out.println("表达式"+str+"的分析过程如下:");
        System.out.printf("%-5s %-15s %-6s %-16s %-20s \n", "步骤", "分析栈", "当前输入", "剩余输入串", "所用产生式");
        StringBuilder stringBuilder = new StringBuilder(str);
        stack.clear();
        //分析栈初始化,将"#","E"依次压入栈中
        stack.add("#");
        stack.add("E");
        int count = 0;
        while (true) {
            count++;
            Character firstone=stringBuilder.charAt(0);//得到输入串的第一个字符

            //如果分析栈中栈顶元素为'#'且剩余且输入串为"#",表示分析成功，退出当前循环
            if (stack.peek().equals("#") && stringBuilder.toString().equals("#")) {
                System.out.printf("%-6d %-20s %-6s %-20s \n", count, stack.toString(), firstone, stringBuilder.toString());
                System.out.println("分析成功");
                break;
            }
            //如果分析栈中栈顶元素为'#'但是剩余且输入串不为"#",表示分析失败，退出当前循环
            else if (stack.peek().equals("#") && !stringBuilder.equals("#")) {
                System.out.printf("%-6d %-20s %-6s %-20s \n", count, stack.toString(), firstone, stringBuilder.toString());
                System.out.println("分析失败");
                break;
            }
            //当分析栈的栈顶元素与剩余输入串的第一个元素相同时，弹出栈顶元素，剩余输入串去掉第一个元素
            else if (stack.peek().equals(String.valueOf(firstone))) {
                System.out.printf("%-6d %-20s %-6s %-20s \n", count, stack.toString(), firstone, stringBuilder.toString());
                stack.pop();
                stringBuilder = stringBuilder.deleteCharAt(0);

            }
            //当分析栈的栈顶元素与剩余输入串的一个元素不相同时，此时弹出栈顶元素，再根据其产生的表达式将元素压入栈中
            else if (!stack.peek().equals(firstone)) {
                String top = stack.peek();
                int row = VNlist.indexOf(top);
                int col = VTlist.indexOf(firstone);
                String temp = analysistable[row][col];//得到该字符在预测分析表中对应的产生式
                StringBuilder expression = new StringBuilder().append(top).append("→").append(temp);
                System.out.printf("%-6d %-20s %-6s %-20s %-20s \n", count, stack.toString(), firstone, stringBuilder.toString(), expression.toString());

                stack.pop();
                if(temp.equals(String.valueOf('ε')))continue;
                //将产生式从后往前依次压入栈中
                for(int i=temp.length()-1;i>=0;i--){
                    if(temp.charAt(i)==')'||temp.charAt(i)=='(')continue;
                    if(temp.charAt(i)=='\''){
                        stack.add(String.valueOf(temp.substring(i-1,i+1)));
                        i--;
                        continue;
                    }
                    stack.add(String.valueOf(temp.charAt(i)));
                }

            }
        }

    }

    public static void main(String[] args) {
        //需要分析的表达式
        String string="i+i*i";
        string=string.concat("#");
        GrammaticalAnalysis testExp1=new GrammaticalAnalysis();
        testExp1.process(string);
    }

}