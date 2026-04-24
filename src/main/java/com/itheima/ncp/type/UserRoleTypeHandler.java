package com.itheima.ncp.type;

import com.itheima.ncp.entity.user.UserRole;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 从数据库读取用户角色时容错：非法/空串不再交给默认 {@link org.apache.ibatis.type.EnumTypeHandler} 抛未解析异常
 * 导致整接口 500；无识别值时返回 null，由业务层与登录态给出明确提示或修复数据。
 */
@MappedTypes(UserRole.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.CHAR})
public class UserRoleTypeHandler extends BaseTypeHandler<UserRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserRole parameter, JdbcType jdbcType) throws SQLException {
        // 写库时统一使用枚举名字符串。
        ps.setString(i, parameter.name());
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从列名读取并做容错解析。
        return parse(rs.getString(columnName));
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从列索引读取并做容错解析。
        return parse(rs.getString(columnIndex));
    }

    @Override
    public UserRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 存储过程结果读取并做容错解析。
        return parse(cs.getString(columnIndex));
    }

    private static UserRole parse(String v) {
        // null 直接返回 null。
        if (v == null) {
            return null;
        }
        // 去空白后判空。
        String t = v.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            // 统一转大写再映射枚举。
            return UserRole.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 非法值容错为 null，由业务层决定后续处理。
            return null;
        }
    }
}
