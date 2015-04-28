package ru.mremne.service;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import ru.mremne.model.mongo.dao.Product;
import ru.mremne.model.mongo.dao.User;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;

import static ru.mremne.model.mongo.dao.ProductDAO.getProductDAO;
import static ru.mremne.model.mongo.dao.UserDAO.getUserDAO;

/**
 * autor:maksim
 * date: 23.04.15
 * time: 14:54.
 */
@Resource
@ManagedBean
public class MongoServiceImpl implements MongoService {
    @Override
    public void addProduct(Product product) {
       getProductDAO().save(product);

    }

    @Override
    public Product getProductById(ObjectId id) {

        return null;
    }

    @Override
    public Query<Product> getAllProducts() {
        Query<Product> products=getProductDAO().getDs().find(Product.class);
        return products;
    }

    @Override
    public Query<User> getAllUsers() {
        Query<User> users=getUserDAO().getDs().find(User.class);
        return users;
    }

    @Override
    public void addUser(User user) {
        getUserDAO().save(user);

    }
}