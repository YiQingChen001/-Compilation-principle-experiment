import java.io.*;
import java.util.*;

public class AutoGrammer {

    //保存所有的文法
    private List<String> list=new ArrayList<>();
    //保存每一个非终结符的产生式
    private Map<String,List<String>> analysismap=new HashMap<>();
    //预测分析表
    private String[][] analysistable = null;
    //非终结符集合的字符串
    private List<String> VNlist=new ArrayList<>();
    //终结符集合的字符串
    private List<Character> VTlist=new ArrayList<>();
    //First集
    private Map<String, List<Character>> firstMap = new HashMap<>();
    //Follow集
    private Map<String, List<Character>> followMap = new HashMap<>();
    //分析栈
    private Stack<String> stack = new Stack<>();

    /**
     * 得到G(E)文法
     * 该文法符合LL(1)文法
     * 无左递归的情况
     */
    public void getGE(){
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File("Grammer.txt").getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(fileReader);
        String source=null;
        while(true){
            try {
                if (!((source=reader.readLine())!=null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            list.add(source);
        }
        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i));
        }
    }

   //得到每一个非终结符的所有产生式
    public void getanalysismap(){
        for(int i=0;i<list.size();i++){
            String temp=list.get(i);
            int index=temp.indexOf('→');
            if(!analysismap.containsKey(temp.substring(0,index))){
                ArrayList<String> ans = new ArrayList<>();
                ans.add(temp.substring(index+1));
                analysismap.put(temp.substring(0,index),ans);
            }
            else analysismap.get(temp.substring(0,index)).add(temp.substring(index+1));
        }
    }

    //获取非终结符集的字符串
    public void getVN(){
        for(int i=0;i<list.size();i++){
            String temp=list.get(i);
            int index=temp.indexOf('→');
            VNlist.add(temp.substring(0,index));
        }
//        for(int i=0;i<VNlist.size();i++) System.out.println(VNlist.get(i));
    }
    //获取终结符集的字符串
    public void getVT(){
        for(int i=0;i<list.size();i++){
            String temp=list.get(i);
            int index=temp.indexOf('→');
            String tempend=temp.substring(index+1);
            for(int j=0;j<tempend.length();j++){
                char ch=tempend.charAt(j);
                if(VNlist.contains(String.valueOf(ch))||ch=='|'||ch=='\'')continue;
                if(!VTlist.contains(ch))
                VTlist.add(ch);
            }
        }
//        for(int i=0;i<VTlist.size();i++) System.out.println(VTlist.get(i));
    }
    //初始化，终结符的First集是自己本身//初始化，终结符的First集是自己本身
    public void initFirst(){
        for (int i = 0; i < VTlist.size(); i++) {
            firstMap.put(String.valueOf(VTlist.get(i)), Arrays.asList(VTlist.get(i)));
        }
    }
    //获取First集
    private Set<Character> getFirst(String str) {
        List<String> stringList = analysismap.get(str);
        HashSet<Character> set = new HashSet<>();
        Iterator<String> it = stringList.iterator();
        while (it.hasNext()) {
            String next = it.next();
            String[] splitarr= next.split("\\|");
            for (String temp : splitarr) {
                //如果产生式的第一个字符是终结符，则将该终结符加入到该非终结符的First集中
                if (VTlist.contains(temp.charAt(0))) {
                    set.add(temp.charAt(0));
                }
                else {
                    int count=0;
                    //递归检查该字符的First集是否含有空集，如果没有，结束；否则加入到开始的First集中，继续检查下一个
                    while(true){
                        if(count>=temp.length())break;
                        Set<Character> oneset = getFirst(String.valueOf(temp.charAt(count++)));
                        set.addAll(oneset);
                        if(!oneset.contains('ε'))break;
                    }
                }
            }
        }
        return set;
    }
    //得到所有非终结符的First集
    public void getAllFirst(){
        for(int i=0;i<VNlist.size();i++){
            String str = VNlist.get(i);
            Set<Character> res = getFirst(str);
            firstMap.put(str, new ArrayList<>(res));
        }
    }

    //获取Follow集
    public Set<Character> getFollow(String str) {
        /**
         * FOLLOW集是从产生式右部找；对于FOLLOW(A)，如果有一个产生式右部存在A，大致分四种情况：
         *
         * A后面没有其他符号，那么FOLLOW(A) = FOLLOW（产生式右部符号）；
         * A后面是一个终结符，那么就把这个终结符加入FOLLOW(A)集合中去；
         * A后面是一个非终结符且这个非终结符的FIRST集中不含空串，那么就把这个非终结符的FIRST集加入到FOLLOW(A)中；
         * A后面是一个非终结符且这个非终结符的FIRST集中含有空串，那么把这个非终结符的FIRST集减去空串后，加入FOLLOW(A)，
         * 并且，产生式右部的FOLLOW集也加入到FOLLOW(A)中；
         */
        if(followMap.containsKey(str)){
            return new HashSet<>(followMap.get(str));
        }
        Set<Character> set = new HashSet<>();
        //如果是开始字符，则将'#'加入到其Follow集中
        if(str.equals(VNlist.get(0)))set.add('#');

        for(int i=0;i<VNlist.size();i++){
            List<String> sList = analysismap.get(VNlist.get(i));
            Iterator<String> iterator = sList.iterator();
            while(iterator.hasNext()){
                String next = iterator.next();
                String[] splitarr = next.split("\\|");
                for (String temp : splitarr) {
                    int indexf=temp.indexOf(str);
                    int interval=str.length();
                    if(indexf==-1)continue;

                    int indexend=indexf+interval-1;
                    if(indexend<temp.length()-1){
                        if(temp.charAt(indexend+1)=='\'')continue;
                    }
                    //A后面没有其他符号，那么Follow（产生式右部符号）加入到Follow(A)中
                    if(indexend==temp.length()-1){
                        if(str.equals(VNlist.get(i)))continue;
                        Set<Character> follow = getFollow(VNlist.get(i));
                        set.addAll(follow);
                    }
                    else{
                        //A后面是一个终结符，那么就把这个终结符加入FOLLOW(A)集合中去；
                        if(VTlist.contains(temp.charAt(indexend+1))){
                            set.add(temp.charAt(indexend+1));
                        }
                        //A后面是一个非终结符
                        else{
                            String ans=null;
                            if(indexend<temp.length()-2){
                                if(temp.charAt(indexend+2)=='\'')ans=temp.substring(indexend+1,indexend+3);
                                else ans=String.valueOf(temp.charAt(indexend+1));
                            }
                            else ans=String.valueOf(temp.charAt(indexend+1));
                            Set<Character> first = getFirst(ans);
                            if(first.contains('ε')){
                                first.remove('ε');
                                set.addAll(first);
                                Set<Character> follow1 = getFollow(VNlist.get(i));
                                set.addAll(follow1);
                            }
                            else{
                                set.addAll(first);
                            }
                        }
                    }
                }
            }
        }
        return set;
    }
    //得到所有的非终结符的Follow集
    public void getAllFollow(){

        for(int i=0;i<VNlist.size();i++){
            String str = VNlist.get(i);
            Set<Character> res = getFollow(str);
            followMap.put(str, new ArrayList<>(res));
        }
    }


    //打印First集
    public void printFirst() {
        System.out.println("First集如下:");
        for(int i=0;i<VNlist.size();i++){
            StringBuilder res= new StringBuilder("First").append('(').append(VNlist.get(i)).append(") = {");
            List<Character> characters = firstMap.get(VNlist.get(i));
            for(int j=0;j<characters.size();j++){
                if (j == characters.size() - 1) res.append(characters.get(j)).append('}');
                else res.append(characters.get(j)).append(',');
            }
            System.out.println(res.toString());
        }
    }
    //打印非终结符集和终结符集
    public void printVNandVT(){
        System.out.println("非终结符集为："+VNlist.toString());
        System.out.println("终结符集为："+VTlist.toString());
    }
    //打印Follow集
    public void printFollow(){
        System.out.println("Follow集如下");
        for(int i=0;i<VNlist.size();i++){
            StringBuilder res= new StringBuilder("Follow").append('(').append(VNlist.get(i)).append(") = {");
            List<Character> characters = followMap.get(VNlist.get(i));
            for(int j=0;j<characters.size();j++){
                if (j == characters.size() - 1) res.append(characters.get(j)).append('}');
                else res.append(characters.get(j)).append(',');
            }
            System.out.println(res.toString());
        }
    }

    //获取预测分析表
    public void getAnalysisTable() {

        /**
         * for(非终结符A:非终结符集)
         * {
         *       获取FIRST(A)
         *       对于FIRST集的每一个元素
         *       找到对应产生式，加入分析表
         *       如果FIRST集包含空串，则对于任意b（b∈vt）属于FOLLOW(A)，A->空串加入到M[A,b]
         * }
         */
        analysistable=new String[VNlist.size()][VTlist.size()+1];
        VTlist.add('#');
        for(int i=0;i<VNlist.size();i++){
            List<Character> characters = firstMap.get(VNlist.get(i));
            //如果其First集中不含有空集
            if(!characters.contains('ε')){
                for (Character ch : characters) {
                    int index=VTlist.indexOf(ch);
                    List<String> stringList = analysismap.get(VNlist.get(i));
                    for (String str : stringList) {
                        String[] split = str.split("\\|");
                        //如果产生式中含有'|'，则将与First集中对应的产生式（产生式的第一个字符与First集中的对应）赋值给预测分析表的对应位置
                        if(split.length>1){
                            for (String s : split) {
                                if(s.indexOf(ch)==0){
                                    StringBuilder stringBuilder = new StringBuilder(VNlist.get(i)).append("→").append(s);
                                    analysistable[i][index]=stringBuilder.toString();
                                }
                            }
                        }
                        //如果产生式中含有'|'，直接将产生式赋值给预测分析表
                        else{
                            StringBuilder stringBuilder = new StringBuilder(VNlist.get(i)).append("→").append(split[0]);
                            analysistable[i][index]=stringBuilder.toString();
                        }
                    }
                }
            }
            //如果其First集中含有空集,则移除空集，
            else{
                int ind=characters.indexOf('ε');
                characters.remove(ind);
                for (Character ch : characters) {
                    int index=VTlist.indexOf(ch);
                    List<String> stringList = analysismap.get(VNlist.get(i));
                    for (String str : stringList) {
                        String[] split = str.split("\\|");
                        for (String s : split) {
                            if(s.indexOf(ch)==0)analysistable[i][index]=s;
                        }
                    }
                }

                List<Character> characters1 = followMap.get(VNlist.get(i));
                StringBuilder temp = new StringBuilder(VNlist.get(i)).append("→ε");
                for (Character ch : characters1) {
                    int index=VTlist.indexOf(ch);
                    analysistable[i][index]=temp.toString();
                }
            }
        }
    }
    //打印预测分析表
    public void printAnalysisTable(){
        System.out.println("预测分析表如下:");
//        for(int i=0;i<analysistable.length;i++)
//            System.out.println(Arrays.toString(analysistable[i]));
        System.out.print("      ");
        for(int i=0;i<VTlist.size();i++)System.out.print(VTlist.get(i)+"\t\t");
        System.out.println();
        for(int i=0;i<analysistable.length;i++) {
            System.out.print(VNlist.get(i)+"\t");
            for(int j=0;j<analysistable[i].length;j++) {
                System.out.printf(analysistable[i][j]+"\t");
            }
            System.out.println();
        }
    }


    //分析表达式的全部过程
    public void process(String str) {

        System.out.println("表达式"+str+"的分析过程如下:");
        System.out.printf("%-5s %-15s %-6s %-16s %-20s \n", "步骤", "分析栈", "当前输入", "剩余输入串", "所用产生式");
        StringBuilder stringBuilder = new StringBuilder(str);
        stack.clear();
        //将分析栈初始化
        stack.add("#");
        stack.add(VNlist.get(0));
        int count = 0;
        while (true) {
            count++;
            Character firstone=stringBuilder.charAt(0);

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
                String temp = analysistable[row][col];
                int index=temp.indexOf('→');
                temp=temp.substring(index+1);
                StringBuilder expression = new StringBuilder().append(top).append("→").append(temp);
                System.out.printf("%-6d %-20s %-6s %-20s %-20s \n", count, stack.toString(), firstone, stringBuilder.toString(), expression.toString());

                stack.pop();
                if(temp.equals(String.valueOf('ε')))continue;
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
        AutoGrammer autoGrammer = new AutoGrammer();
        autoGrammer.getGE();            //从txt文件获取G(E)文法
        autoGrammer.getVN();            //得到非终结符集
        autoGrammer.getVT();            //得到终结符集
        autoGrammer.printVNandVT();     //打印非终结符集和终结符集
        autoGrammer.getanalysismap();   //得到每一个非终结符的产生式
        autoGrammer.initFirst();        //初始化First集
        autoGrammer.getAllFirst();      //得到First集
        autoGrammer.printFirst();       //打印First集
        autoGrammer.getAllFollow();     //得到Follow集
        autoGrammer.printFollow();      //打印Follow集
        autoGrammer.getAnalysisTable(); //得到预测分析表
        autoGrammer.printAnalysisTable();//打印预测分析表
        autoGrammer.process("i+i#"); //打印该表达式的分析过程
    }
}