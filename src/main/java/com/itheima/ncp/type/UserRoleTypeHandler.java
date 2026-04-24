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
        ps.setString(i, parameter.name());
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public UserRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public UserRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private static UserRole parse(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return UserRole.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
