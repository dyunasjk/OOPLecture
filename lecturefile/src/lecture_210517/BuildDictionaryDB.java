package lecture_210517;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BuildDictionaryDB extends JPanel implements ActionListener{
   
    private JTextField inputField =new JTextField(30);
    private JButton searchBtn = new JButton("검색");
    private JButton addBtn =new JButton("추가");
   
    private Map<String, String> dictMap = new HashMap<>();
    private static final String DIC_FILE_NAME="resources/dict.oop";
    private static final String JDBC_CLASS_NAME = "org.mariadb.jdbc.Driver";
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/oop";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

//    private static final String 
   
    public BuildDictionaryDB() {
        this.add(inputField);
        this.add(searchBtn);
        this.add(addBtn);
        searchBtn.addActionListener(this);
        addBtn.addActionListener(this);
        this.setPreferredSize(new Dimension(600,50));   
        //JDBC 드라이버 메모리 적재하기
        //JDBC 클래스 이름은 DB마다 다름
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            //buildDictionaryFromFile();
            buildDictionaryFromDB();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void buildDictionaryFromDB() {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_PASSWORD, DB_PASSWORD)) {
            String sql = "select * from dict";
            // preparedStatement 객체는 실행준비가 완료된 sql 객체
            PreparedStatement pstmt = con.preparedStatement(sql);
            // Insert, delete,update 문의 실행은
            // executeUpdate() 메소드를 호출하고
            // select 문의 실행은 executeQuery() 메소드를 호출
            ResultSet rs = pstmt.executeQuery();
            while (rs.next) {
                String key = rs.getString("word1");
                String value = rs.getString("word2");

                System.out.println(key + " : " + value);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private void BuildDictionaryDBFromFile(){
        Properties props = new Properties();
        try(FileReader fReader = new FileReader(DIC_FILE_NAME)){
            props.load(fReader);   
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Set<Object> set = props.keySet();
        for(Object key : set) {
            Object value = props.get(key);
            dictMap.put((String)key,(String)value);
        }
    }
    
    //   @Override
    //   public void actionPerformed(ActionEvent e) {
    //      String key = inputField.getText();
    //      String value =dictMap.get(key);
    //      if(value!=null )
    //      if(e.getSource() == searchBtn) {
    //         if(value==null)
    //         JOptionPane.showMessageDialog(this,"단어를 찾을수 없습니다",key,JOptionPane.ERROR_MESSAGE);
    //         else {
    //            JOptionPane.showMessageDialog(this,value,key,JOptionPane.INFORMATION_MESSAGE);
    //         }
    //      }
    //      else if (e.getSource() ==addBtn) {
    //         String Value = JOptionPane.showInputDialog(this, key +" 에 대응되는 영어단어를 입력하세요");
    //         System.out.println(key+value);
    //         if(Value.trim().length() == 0) return;
    //         dictMap.put(key, Value);
    //         addWordToFile(key,Value);
    //         JOptionPane.showMessageDialog(this, "영어단어 추가 되었습니다","성공",JOptionPane.INFORMATION_MESSAGE);
    //         
    //      
    //      }
    //      inputField.requestFocus();
    //   
    //   }
    @Override
    public void actionPerformed(ActionEvent e) {
        String key = inputField.getText();
        String value =dictMap.get(key);
        if(e.getSource() == searchBtn) {
            if(value==null)
            JOptionPane.showMessageDialog(this,"단어를 찾을수 없습니다",key,JOptionPane.ERROR_MESSAGE);
            else {
                JOptionPane.showMessageDialog(this,value,key,JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else if (e.getSource() ==addBtn) {
            String Value = JOptionPane.showInputDialog(this, key +" 에 대응되는 영어단어를 입력하세요");
            System.out.println(key+value);
            if(Value.trim().length() == 0) return;
            dictMap.put(key, Value);
            //addWordToFile(key,Value);
                addWordToDB(key,Value);
            JOptionPane.showMessageDialog(this, "영어단어 추가 되었습니다","성공",JOptionPane.INFORMATION_MESSAGE);
            
        
        }
        inputField.requestFocus();
    
    }
    private void addWordToDB(String Key, String Value) {
        
        
    }
    private void addWordToFile(String key,String value) {
        try(FileWriter fWriter = new FileWriter(DIC_FILE_NAME)){
            String str = key+"="+value+"\n";
            fWriter.write(str);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        };
    }         
    
    
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.add(new BuildDictionaryDB());
        frame.setTitle("나의 한영사전");
        frame.setResizable(false);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        
    }
    
    
    
    
    }