package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;

public class DatabaseDaoFactory extends DaoFactory {

    @Override
    public AccountDao getAccountDao() {
        return null;
    }

    @Override
    public ProfileDao getProfileDao() {
        return null;
    }

}
