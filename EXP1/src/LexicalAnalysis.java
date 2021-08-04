
import java.util.HashMap;
import java.util.Map;

public class LexicalAnalysis {

    public static String converge(String str) {

        //使用HashMap来保存关键字和专用字符与之对应的种别码
        Map<String,Integer> map=new HashMap<>();

        map.put("main",1);
        map.put("int",2);
        map.put("char",3);
        map.put("if",4);
        map.put("else",5);
        map.put("for",6);
        map.put("while",7);
        map.put("return",8);
        map.put("void",9);
        map.put("STRING",50);
        map.put("ID",10);
        map.put("INT",20);
        map.put("=",21);
        map.put("+",22);
        map.put("-",23);
        map.put("*",24);
        map.put("/",25);
        map.put("(",26);
        map.put(")",27);
        map.put("[",28);
        map.put("]",29);
        map.put("{",30);
        map.put("}",31);
        map.put(",",32);
        map.put(":",33);
        map.put(";",34);
        map.put(">",35);
        map.put("<",36);
        map.put(">=",37);
        map.put("<=",38);
        map.put("==",39);
        map.put("!=",40);
        StringBuilder result=new StringBuilder();//保存结果
        int index=0;
        while(index<str.length()) {
            //如果识别到空格或换行符\r\n就跳过
            while(str.charAt(index)==' '||str.charAt(index)=='\r'||str.charAt(index)=='\n') {
                index++;
            }
            int end=index;
            int ans;

            //用空格分隔符号
            while(str.charAt(end)!=' ') {
                end++;
                if(end==str.length())break;
            }
            String temp=str.substring(index, end);
            if(map.containsKey(temp)) {//如果是关键字，直接在map中获取其种别码
                ans=map.get(temp);
            }

            else if(Character.isDigit(temp.charAt(0))) {//如果单词符号是数字INT
                ans=20;
            }
            else if(Character.isLetter(temp.charAt(0))) {//如果单词符号是ID
                ans=10;
            }
            else {//如果单词符号是STRING
                ans=50;
            }

            result.append('(').append(ans).append(',').append(temp).append(')').append('\n');
            index=++end;

        }
        return result.toString();
    }

    public static void main(String[] args) {
        String temp="if ( a = 1 ) return true ; \r\n" +
                "else return false ;";
        System.out.println("输入的源程序字符串为: \n"+temp);
        String string=temp.trim();//去掉字符串两端的空格
        System.out.println();
        System.out.println(converge(string));

    }

}


