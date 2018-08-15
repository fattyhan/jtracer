package com.jdjr.tracer.db;

import com.jdjr.tracer.constant.InvokeType;
import com.jdjr.tracer.entity.ReturnMsg;
import com.jdjr.tracer.constant.Segment;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHelper {

    /**
     * Constructor
     */
    private static Morphia morphia = new Morphia();
    private static Datastore datastore = null;

    private static Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    //TODO 封装增改查方法，当区间调用链完成时将RPCUUID填充到本区间的所有节点
    public DatabaseHelper() {
        if (!morphia.isMapped(ReturnMsg.class)) {
            morphia.map(ReturnMsg.class);
            initDatastore();
        } else {
            logger.info("Database Class Mapped Already!");
        }
    }


    void initDatastore() {

        ProcessBuilder processBuilder = new ProcessBuilder();
        MongoClient mongoClient;

        String HEROKU_MLAB_URI = processBuilder.environment().get("MONGODB_URI");

        if (HEROKU_MLAB_URI != null && !HEROKU_MLAB_URI.isEmpty()) {
            logger.error("Remote MLAB Database Detected");
            mongoClient = new MongoClient(new MongoClientURI(HEROKU_MLAB_URI));
            datastore = morphia.createDatastore(mongoClient, Path.Database.HEROKU_DB_NAME);
        } else {
            logger.info("Local Database Detected");
            mongoClient = new MongoClient(Path.Database.HOST, Path.Database.PORT);
            datastore = morphia.createDatastore(mongoClient, Path.Database.LOCAL_DBNAME);
        }

        datastore.ensureIndexes();
        logger.info("Database connection successful and Datastore initiated");
    }


    public Datastore getDataStore() {
        if (datastore == null) {
            initDatastore();
        }

        return datastore;
    }

    public void unifyRPCUuid(String traceId) {
        //--查询同traceID的起始节点
        ReturnMsg _rootR = getDataStore().createQuery(ReturnMsg.class)
                .field("traceId").equal(traceId)
                .field("segmentId").equal(Segment.ONE)
                .field("invokeType").equal(InvokeType.RPC)
                .field("RPCUuid").exists()
                .get();
        //--拿到起始节点的PRCUUID根据traceID全部更新本RPC区间内的记录
        if (_rootR != null) {
            Query<ReturnMsg> _rq = getDataStore().createQuery(ReturnMsg.class)
                    .filter("traceId =", _rootR.getTraceId());
            UpdateOperations<ReturnMsg> _rp = getDataStore().createUpdateOperations(ReturnMsg.class)
                    .set("RPCUuid", _rootR.getRPCUuid());

            getDataStore().update(_rq, _rp);
        }
    }

    public void updateElapsedTime(String traceId, String segmentId,String elapsedTime) {
        //--根据traceId和segmentId更新
        Query<ReturnMsg> _rq = getDataStore().createQuery(ReturnMsg.class)
                .filter("traceId =", traceId)
                .filter("segmentId =",segmentId);
        UpdateOperations<ReturnMsg> _rp = getDataStore().createUpdateOperations(ReturnMsg.class)
                .set("elapsedTime", elapsedTime);

        getDataStore().update(_rq, _rp);
    }

}
