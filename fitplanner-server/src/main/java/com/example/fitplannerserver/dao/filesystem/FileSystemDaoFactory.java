package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.dao.SessionLogDao;

public class FileSystemDaoFactory extends DaoFactory {
    @Override
    public AccountDao getAccountDao() {
        return null;
    }

    @Override
    public ProfileDao getProfileDao() {
        return null;
    }

    @Override
    public SessionLogDao getSessionLogDao() {
        return null;
    }


}
