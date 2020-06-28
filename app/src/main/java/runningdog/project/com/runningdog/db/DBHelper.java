package runningdog.project.com.runningdog.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context) {
        super(context, "/mnt/sdcard/"+"runningdog.db", null, 1);
        this.context = context;
    }

    /**
     * Database가 존재하지 않을 때, 딱 한번 실행된다.
     * DB를 만드는 역할을 한다.
     * @param db */

    @Override
    public void onCreate(SQLiteDatabase db) {
        // String 보다 StringBuffer가 Query 만들기 편하다.

        StringBuffer sb = new StringBuffer();

        sb.append(" CREATE TABLE RUNNING ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(" DATE TEXT, ");
        sb.append(" TIME TEXT, ");
        sb.append("RUN INTEGER ) ");

        // SQLite Database로 쿼리 실행

        db.execSQL(sb.toString());

        Toast.makeText(context, "Table 생성완료", Toast.LENGTH_LONG).show();

    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();
    }

    public void insert(String date, String time, int run) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO RUNNING VALUES(null, '" + date + "', '" + time + "', " + run + ");");
        db.close();
    }

    public void delete(String date, String time) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM RUNNING WHERE DATE='" + date + "' AND TIME ='" + time + "';");
        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM RUNNING", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0) //id
                    + " : "
                    + cursor.getString(1) //data
                    + " : "
                    + cursor.getInt(2) //time
                    + ": "
                    + cursor.getString(3) //run
                    + "\n";
        }

        return result;
    }
}