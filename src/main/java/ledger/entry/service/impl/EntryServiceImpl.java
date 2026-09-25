package ledger.entry.service.impl;

import ledger.entry.dao.EntryDAO;
import ledger.entry.service.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("entryService")
public class EntryServiceImpl implements EntryService {

    @Autowired
    private EntryDAO entryDAO;

    @Override
    public List<Map<String, Object>> selectEntryList(Map<String, Object> param) {
        return entryDAO.selectEntryList(param);
    }

    @Override
    public List<Map<String, Object>> selectRecentEntries(Map<String, Object> param) {
        return entryDAO.selectRecentEntries(param);
    }

    @Override
    public Map<String, Object> selectEntry(Map<String, Object> param) {
        return entryDAO.selectEntry(param);
    }

    @Override
    public int insertEntry(Map<String, Object> param) {
        return entryDAO.insertEntry(param);
    }

    @Override
    public int updateEntry(Map<String, Object> param) {
        return entryDAO.updateEntry(param);
    }

    @Override
    public int deleteEntry(Map<String, Object> param) {
        return entryDAO.deleteEntry(param);
    }
}
