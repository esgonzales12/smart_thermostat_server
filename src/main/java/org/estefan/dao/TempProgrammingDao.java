package org.estefan.dao;

import java.util.List;
import java.util.Optional;

public class TempProgrammingDao extends DaoBase {

    private static TempProgrammingDao instance;

    private TempProgrammingDao() {}

    public static synchronized TempProgrammingDao getInstance() {
        if (instance == null) {
            instance = new TempProgrammingDao();
        }
        return instance;
    }

    public List<TempProgrammingRecord> findByDeviceId(Integer deviceId) {
        String sql = String.format("""
                SELECT * FROM tempPrograms
                WHERE deviceId = '%d';
                """, deviceId);
        return selectMany(TempProgrammingRecord.class, sql);
    }

    public Optional<TempProgrammingRecord> findByRecordId(Integer recordId) {
        String sql = String.format("""
                SELECT * FROM tempPrograms
                WHERE id = '%d';
                """, recordId);
        return selectOne(TempProgrammingRecord.class, sql);
    }

    public boolean deleteRecord(Integer recordId) {
        return delete("tempPrograms", "id", String.valueOf(recordId)) > 0;
    }

    public TempProgrammingRecord saveRecord(TempProgrammingRecord record) {
        String sql = String.format("""
                INSERT INTO tempPrograms (deviceId, datetime, temp)
                VALUES ('%s', '%s', '%s')
                """, record.getDeviceId(), record.getDatetime(), record.getTemp());
        return create(TempProgrammingRecord.class, sql);
    }
}
