package pl.lstypka.springSecurityDistributedSystem.config.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;
import org.springframework.util.SerializationUtils;

import javax.transaction.Transactional;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JPASessionRepository implements SessionRepository<ExpiringSession> {

    private SpringSessionRepository springSessionRepository;

    public JPASessionRepository(SpringSessionRepository springSessionRepository) {
        this.springSessionRepository = springSessionRepository;
    }

    @Override
    public ExpiringSession createSession() {
        return new MapSession();
    }

    @Transactional
    @Override
    public void save(ExpiringSession expiringSession) {
        Function<ExpiringSession, SessionEntity> sessionToEntity = session -> {
            SessionEntity sessionEntity = new SessionEntity();
            sessionEntity.setId(session.getId());
            sessionEntity.setLastAccessedTime(session.getLastAccessedTime());
            sessionEntity.setCreationTime(session.getCreationTime());
            Map<String, Object> attributes = session.getAttributeNames().stream().collect(Collectors.toMap(name -> name, name -> session.getAttribute(name)));
            sessionEntity.setData(SerializationUtils.serialize(new SessionAttributes(attributes)));

            return sessionEntity;
        };

        springSessionRepository.save(sessionToEntity.apply(expiringSession));
    }

    @Transactional
    @Override
    public ExpiringSession getSession(String id) {
        SessionEntity sessionEntity = springSessionRepository.findOne(id);
        if (sessionEntity == null) {
            return null;
        }

        Function<SessionEntity, ExpiringSession> entityToSession = entity -> {
            MapSession session = new MapSession();
            session.setId(entity.getId());
            session.setLastAccessedTime(entity.getLastAccessedTime());
            session.setCreationTime(entity.getCreationTime());
            SessionAttributes attributes = (SessionAttributes) SerializationUtils.deserialize(entity.getData());
            attributes.getAttributes().entrySet().stream().forEach(entry -> session.setAttribute(entry.getKey(), entry.getValue()));
            return session;
        };

        ExpiringSession saved = entityToSession.apply(sessionEntity);
        if (saved.isExpired()) {
            delete(saved.getId());
            return null;
        }
        return saved;
    }


    @Override
    public void delete(String id) {
        springSessionRepository.delete(id);
    }


}