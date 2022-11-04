package kitchenpos.table.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.domain.TableGroupRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TableGroupJdbcRepository implements TableGroupRepository {

    private final JdbcTemplateTableGroupDao tableGroupDao;
    private final JdbcTemplateOrderTableDao orderTableDao;

    public TableGroupJdbcRepository(final JdbcTemplateTableGroupDao tableGroupDao,
                                    final JdbcTemplateOrderTableDao orderTableDao) {
        this.tableGroupDao = tableGroupDao;
        this.orderTableDao = orderTableDao;
    }

    @Override
    public TableGroup save(final TableGroup entity) {
        TableGroup tableGroup = tableGroupDao.save(entity);
        List<OrderTable> orderTables = entity.getOrderTables().stream()
                .map(orderTable -> new OrderTable(orderTable.getId(), orderTable.getNumberOfGuests(), false))
                .map(orderTable -> orderTableDao.save(orderTable, tableGroup.getId()))
                .collect(Collectors.toList());
        return new TableGroup(tableGroup.getId(), tableGroup.getCreatedDate(), orderTables);
    }

    @Override
    public Optional<TableGroup> findById(final Long id) {
        return tableGroupDao.findById(id);
    }

    @Override
    public List<TableGroup> findAll() {
        List<TableGroup> tableGroups = tableGroupDao.findAll();

        return tableGroups.stream().map(tableGroup -> new TableGroup(tableGroup.getId(), tableGroup.getCreatedDate(),
                orderTableDao.findAllByTableGroupId(tableGroup.getId()))).collect(Collectors.toList());
    }
}
