package kitchenpos.menu.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProduct;
import org.springframework.stereotype.Repository;

@Repository
public class MenuRepository implements MenuDao {

    private final JdbcTemplateMenuDao jdbcTemplateMenuDao;
    private final JdbcTemplateMenuProductDao jdbcTemplateMenuProductDao;

    public MenuRepository(final JdbcTemplateMenuDao jdbcTemplateMenuDao,
                          final JdbcTemplateMenuProductDao jdbcTemplateMenuProductDao) {
        this.jdbcTemplateMenuDao = jdbcTemplateMenuDao;
        this.jdbcTemplateMenuProductDao = jdbcTemplateMenuProductDao;
    }

    @Override
    public Menu save(final Menu entity) {
        Menu menu = jdbcTemplateMenuDao.save(entity);
        List<MenuProduct> menuProducts = entity.getMenuProducts()
                .stream()
                .map(menuProduct -> new MenuProduct(menuProduct.getProductId(), menuProduct.getQuantity()))
                .map(menuProduct -> jdbcTemplateMenuProductDao.save(menuProduct, menu.getId()))
                .collect(Collectors.toList());

        return new Menu(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getMenuGroupId(),
                menuProducts
        );
    }

    @Override
    public Optional<Menu> findById(final Long id) {
        return jdbcTemplateMenuDao.findById(id);
    }

    @Override
    public List<Menu> findAll() {
        List<Menu> menus = jdbcTemplateMenuDao.findAll();

        return menus.stream()
                .map(menu -> new Menu(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getMenuGroupId(),
                        jdbcTemplateMenuProductDao.findAllByMenuId(menu.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public long countByIdIn(final List<Long> ids) {
        return jdbcTemplateMenuDao.countByIdIn(ids);
    }
}
