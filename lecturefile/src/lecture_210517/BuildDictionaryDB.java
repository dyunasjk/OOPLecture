package Lecture_210517;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SimpleDictionary extends JPanel implements ActionListener {

    // 단어 입력 받을 수 있는 JTextField
    // 검색 버튼, 추가 버튼
    // 단어장 구현을 위한 자료 구조로 Map 객체 생성

    private JTextField inputField = new JTextField(30);
    private JButton searchBtn = new JButton("검색");
    private JButton addBtn = new JButton("추가");
    private static final String driverClassName = "org.mariadb.jdbc.Driver";

    // DB마다 서버 URL 포맷이 달라 db마다 찾아보아야 한다
    private static final String DB_SERVER_URL = "jdbc:mysql://localhost:3306/oop";
    private static final String DB_USER = "root";
    private static final String DB_USER_PW = "Dyun@s1.2.3.";

    // Map객체를 단어장 구현으로 사용
    // key는 한글단어, value는 대응되는 영어 단어를 저장한다
    private Map<String, String> words = new HashMap<>();
    private static final String DIC_FILE_NAME = "dict.props";

    public SimpleDictionary() {
        // Panel의 기본 레이아웃은 FlowLayout
        this.add(inputField);
        this.add(searchBtn);
        this.add(addBtn);

        // 각 버튼에 글릭 이벤트가 발생 했을때 처리할 리스너를 지정
        searchBtn.addActionListener(this);
        addBtn.addActionListener(this);

        this.setPreferredSize(new Dimension(600, 50));

        // 파일에 key=value 형태로 저장된 엔트리들을 읽어서, words를 구성하기
        // DB에서 레코드를 읽고, 그 레코드를 이용해서 dict맵을 구성한다.

        buildDictionaryFromDB();

        // buildDictionaryFromFile();
    }

    private void buildDictionaryFromDB() {
        // 1. Database 연결: 먼저 JDBC Driver를 로딩 해야 한다
        // DriverManager(java.sql 패키지에 정의된 클래스)
        // Connection con = DriverManager.getConnection();
        // 메서드를 호출해 연결을 establish한다
        // 이 때 연결 정보를 getConnection() 메서드에 전달 해 줘야 한다
        // 연결 정보: DB Server의 URL(IP주소와 port 번호포함), DB사용자의 ID와 암호
        // 2. Connection 객체를 통해 SQL문 실행을 서버에 요청하고 그 결과를 받아 처리한다.
        // 두가지 방법이 있다
        // 정적 SQL문: 프로그래밍 시점에 실행할 SQL문이 결정되고 고정된 경우.
        // 첫번째는 con.createStatement() 메소드 호출을 통해서 반환되는 Statement 객체를 이용하는 방법
        // 두번째는 con.perareStatement() 메서드 호출을 통해 반환되는 PreparedStatement 객체를 이용하는 방법이 있다.
        // 동적 SQL문: 프로그래밍 시점에 실행할 SQL문이 결정되지 않고 계속 변경되는 SQL문
        // 이 예제에서는 PreparedStatement 객체를 이용한다.
        // 서버가 실행 될 준비가 되었다는 의미(문법검사, 정당성 검사, excution plan까지 준비 완료된 단계)
        // String sql = "select * from dict";
        // con.perpareStatement(sql);
        // 을 실행하면 해당 sql을 받아서 실행할 준비를 마치고 그 객체를 반환해준다.
        // PreparedStatement pstmt = con.perpareStatement(sql);
        // pstmt를 가지고 sql문을 실행한다.
        // 실행 준비가 된 PreparedStatement를 실행 시키는 방법은 크게 두가지가 있다
        // 첫번째 방식
        // 실행할 SQL문이 insert, delete update문인 경우
        // pstmt.excuteUpdate();를 실행한다 (숫자 값 반환 수행된 행의 개수 반환)
        // 두번째 방식
        // 실행할 SQL문이 select문인 경우
        // ResultSet rs = pstmt.excuteQuery();
        // 3. DB Server와의 연결을 해제한다
        // con.close();

        // MySQL JDBC 드라이버를 메모리에 적재
        // 드라이버 클래스 이름은 DBMS마다 다르다
        // ClassNotFoundException이 발생할 수 있어 try-catch문으로 오류를 잡아준다

        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            return;
        }

        try (Connection con = DriverManager.getConnection(DB_SERVER_URL, DB_USER, DB_USER_PW)) {

            // DB서버에 연결

            // sql(select문) 실행
            String sql = "select * from dict";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // 현재 포인터가 가리키는 칼럼 값을 빼온다
                // 각 칼럼으 타입에 따라서 호출할 메서드가 달라진다
                // char, varchar 타입의 컬럼은 getString("칼럼이름" 또는 "칼럼위치")
                // int 타입의 칼럼은 getInt()
                // DateTime, Date 타입의 컬럼 값은 getDate()를 호출해야한다
                String han = rs.getString("hword");
                String eng = rs.getString("eword");

                words.put(han, eng);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void buildDictionaryFromFile() {
        // Properties는
        // key, value의 타입이 각각 String, String으로 고정된 일종의 맵이다.
        Properties props = new Properties();
        // props.put("사과", "apple");

        // 파일에서 읽어서 props 객체의 <key, value>쌍을 구성할 수 있다.
        // 파일을 읽으려면 FileReader를 생성해준다

        // FileReader fReader = new FileReader(DIC_FILE_NAME);
        // props.load(fReader);

        try (FileReader fReader = new FileReader(DIC_FILE_NAME)) {
            props.load(fReader);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Set<Object> set = props.keySet();
        for (Object obj : set) {
            words.put((String) obj, (String) props.get(obj));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String key = inputField.getText();
        if (key.trim().length() == 0)
            return;

        if (e.getSource() == searchBtn) {
            // 입력된 단어를 추출하여
            // 그 단어를 key 값으로 가지는 대응된느 맵 엔트리가 있는지 검사 words.get(단어);
            // 그 단어에 대응되는 값이 있으면 JOptionPane.showMessageDialog() 메서드를 호출해서 대응되는 값을 보여준다
            // 값이 없으면 (null) JOptionPane.showMessageDialog() 메서드를 호출해서
            // 단어를 찾을 수 없습니다 라고 출력해준다
            // inputField를 빈 문자열로 설정한다

            System.out.println("[" + key + "]");
            String value = words.get(key);

            if (value != null) {
                // 대응되는 단어가 있는 경우
                JOptionPane.showMessageDialog(this, value, key, JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 대응되는 단어가 없는 경우
                JOptionPane.showMessageDialog(this, "단어를 찾을 수 없습니다.", key, JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == addBtn) {
            // 입력된 단어를 추출
            // JOptionPane.showInputDialog() 메서드를 호출해서 추가할 영어 단어를 입력 받는다
            // words.put(입력 필드에 입력된 단어, inputDialog에 입력된 단어)
            // DB에 <key, value>의 쌍을 하나의 레코드로 저장한다.

            String value = JOptionPane.showInputDialog(this, key, "에 대응 되는 영어 단어를 입력하세요.");

            if (value.trim().length() == 0)
                return;
            words.put(key, value);
            addToDB(key, value);
            addWordToFile(key, value);
            JOptionPane.showMessageDialog(this, "[" + value + "]" + "영어 단어가 추가되었습니다.", key,
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // inputField.setText("");

    }

    private void addToDB(String key, String value) {
        // 1. Database 연결: 먼저 JDBC Driver를 로딩 해야 한다
        // 2. select 문 수행
        // 3. select 문의 수행으로 반환된 레코드를 이용해 dict Map 객체를 구성
        // 4. Database 연결 해제.
    }

    private void addWordToFile(String key, String value) {
        try (FileWriter fWriter = new FileWriter(DIC_FILE_NAME, true)) {
            // FileWriter의 write는 계속 덮어쓰므로 마지막에 추가된 것만 파일에 남는다.
            fWriter.write(key + "=" + value + "\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new SimpleDictionary());

        frame.setTitle("한영사전");
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
    }

}