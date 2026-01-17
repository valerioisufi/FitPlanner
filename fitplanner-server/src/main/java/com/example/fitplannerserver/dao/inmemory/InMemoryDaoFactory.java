package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.DaoFactory;

public class InMemoryDaoFactory extends DaoFactory {

    @Override
    public InMemoryAccountDao getAccountDao() {
        return InMemoryAccountDao.getInstance();
    }
}
