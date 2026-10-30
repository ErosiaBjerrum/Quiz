package bybjerrum.config;

import bybjerrum.entity.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import bybjerrum.utils.Utils;

import java.util.Properties;

public class HibernateConfig {

    static {
    System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private static EntityManagerFactory emf;
    private static EntityManagerFactory emfForTest;
    private static boolean isIntegrationTest = false;

    public static void setTestMode(boolean isTest) {
        HibernateConfig.isIntegrationTest = isTest;
    }

    public static EntityManagerFactory getEntityManagerFactoryForTest(){
        setTestMode(true);
        EntityManagerFactory testEmf = getEntityManagerFactory();
        setTestMode(false);
        return testEmf;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (isIntegrationTest) {
            if (emfForTest == null || !emfForTest.isOpen())
                emfForTest = createEMF();
            return emfForTest;
        } else {
            if (emf == null || !emf.isOpen())
                emf = createEMF();
            return emf;
        }
    }

    public static void shutdownTestEmf() {
        if (emfForTest != null) {
            try {
                if (emfForTest.isOpen()) emfForTest.close();
            } finally {
                emfForTest = null;
            }
        }
    }

    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(Question.class);
        configuration.addAnnotatedClass(Answer.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(Contestant.class);
        configuration.addAnnotatedClass(QuizSession.class);
        configuration.addAnnotatedClass(ContestantQuizSession.class);
        configuration.addAnnotatedClass(User.class);
    }

    private static EntityManagerFactory createEMF() {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();

            setBaseProperties(props);
            if (isIntegrationTest) {
                setTestProperties(props);
            } else if (System.getenv("DEPLOYED") != null) {
                setDeployedProperties(props);
            } else {
                setDevProperties(props);
            }
            configuration.setProperties(props);
            getAnnotationConfiguration(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getDBName() {
        String dbName = System.getenv("DB_NAME");
        if (dbName == null || dbName.isBlank()) {
            dbName = System.getProperty("db.name");
        }
        if (dbName == null || dbName.isBlank()) {
            dbName = Utils.getPropertyValue("db.name", "config.properties");
        }
        return dbName;
    }

    private static void setBaseProperties(Properties props) {
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");
    }

    private static void setDeployedProperties(Properties props) {
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + getDBName());
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
    }

    private static void setDevProperties(Properties props) {
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/" + getDBName());
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
    }

    private static void setTestProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
        props.put("hibernate.connection.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test_db");
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        props.put("hibernate.archive.autodetection", "class");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
    }
}
