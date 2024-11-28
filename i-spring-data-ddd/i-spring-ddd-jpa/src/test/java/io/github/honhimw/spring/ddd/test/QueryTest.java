package io.github.honhimw.spring.ddd.test;

import io.github.honhimw.core.*;
import io.github.honhimw.ddd.jpa.util.PageUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-11-28
 */

public class QueryTest {

    @Test
    @SneakyThrows
    void patternMatching() {
        PersonRepository personRepository = null;
        IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
        Person condition = new Person();
        Name name = new Name();
        name.setFirstName("xxx");
        condition.setName(name);
        Person parent = new Person();
        parent.setId("2");
        condition.setParent(parent);
        iPageRequest.setCondition(condition);
        Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
        PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
    }

    @Test
    @SneakyThrows
    void queryBuilder() {
        PersonRepository personRepository = null;
        IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
        List<ConditionColumn> conditions = new ArrayList<>();
        conditions.add(ConditionColumn.of("name.firstName", "xxx", MatchingType.CONTAINING));
        conditions.add(ConditionColumn.of("parent.id", "2", MatchingType.NULL));
        iPageRequest.setConditions(conditions);
        Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
        PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
    }

    @Test
    @SneakyThrows
    void ordering() {
        PersonRepository personRepository = null;
        IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
        List<OrderColumn> orders = new ArrayList<>();
        orders.add(OrderColumn.of("id", true));
        iPageRequest.setOrders(orders);
        Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
        PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
    }

    public interface PersonRepository extends JpaSpecificationExecutor<Person> {}

    @Data
    public static class Person {
        private String id;
        private Name name;
        private Person parent;
    }

    @Data
    public static class Name {
        private String firstName;
        private String lastName;
    }

}
