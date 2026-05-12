package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.SessionLogDao;

public class InMemoryDaoFactory extends DaoFactory {

    @Override
    public InMemoryAccountDao getAccountDao() {
        return InMemoryAccountDao.getInstance();
    }

    @Override
    public InMemoryProfileDao getProfileDao() {
        return InMemoryProfileDao.getInstance();
    }

    @Override
    public SessionLogDao getSessionLogDao() {
        return InMemorySessionLogDao.getInstance();
    }

}
