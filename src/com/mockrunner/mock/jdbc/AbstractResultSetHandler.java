package com.mockrunner.mock.jdbc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mockrunner.util.SearchUtil;

/**
 * Abstract base class for all <code>ResultSet</code> handlers.
 * Used to coordinate <code>ResultSet</code> objects for a
 * statement. You can use this class to prepare <code>ResultSet</code>
 * objects and update count values that are returned by the
 * <code>execute</code> method of a statement, if the current
 * SQL string matches.
 */
public abstract class AbstractResultSetHandler
{
    private boolean caseSensitive = false;
    private boolean exactMatch = false;
    private MockResultSet globalResultSet;
    private Map resultSetsForStatement = new HashMap();
    private int updateCount = 0;
    private Map updateCountForStatement = new HashMap();
    private Map returnsResultSetMap = new HashMap();
    
    /**
     * Set if specified SQL strings should be handled case sensitive.
     * Defaults to to <code>false</code>, i.e. <i>INSERT</i> is the same
     * as <i>insert</i>.
     * @param caseSensitive enable or disable case sensitivity
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Set if specified SQL statements must match exactly.
     * Defaults to <code>false</code>, i.e. any SQL string
     * does not need to match exactly. If the original statement 
     * is <i>insert into mytable values(?, ?, ?)</i>
     * the string <i>insert into mytable</i> will match this statement.
     * Usually <code>false</code> is the best choice, so
     * prepared <code>ResultSet</code> objects do not have
     * to match exactly the current statements SQL string.
     * @param exactMatch enable or disable exact matching
     */
    public void setExactMatch(boolean exactMatch)
    {
        this.exactMatch = exactMatch;
    }
    
    /**
     * Clears the <code>ResultSet</code> objects.
     */
    public void clearResultSets()
    {
        resultSetsForStatement.clear();
    }
    
    /**
     * Returns the <code>Map</code> of all <code>ResultSet</code>
     * objects, that were added with {@link #prepareResultSet(String, MockResultSet)}.
     * The SQL strings map to the corresponding <code>ResultSet</code>.
     * @return the <code>Map</code> of <code>ResultSet</code> objects
     */
    public Map getResultSetMap()
    {
        return Collections.unmodifiableMap(resultSetsForStatement);
    }
    
    /**
     * Returns the first <code>ResultSet</code> that matches the
     * specified SQL string. Please note that you can modify
     * the search parameters with {@link #setCaseSensitive} and 
     * {@link #setExactMatch}.
     * @param sql the SQL string
     * @return the corresponding {@link MockResultSet}
     */
    public MockResultSet getResultSet(String sql)
    {
        List list = SearchUtil.getMatchingObjects(resultSetsForStatement, sql, getCaseSensitive(), getExactMatch(), true);
        if(null != list && list.size() > 0)
        {
            return (MockResultSet)list.get(0);
        }
        return null;
    }
    
    /**
     * Returns the global <code>ResultSet</code>.
     * The statement returns the global <code>ResultSet</code>
     * if no <code>ResultSet</code> can be found for the current
     * SQL string.
     * @return the global {@link MockResultSet}
     */
    public MockResultSet getGlobalResultSet()
    {
        return globalResultSet;
    }
    
    /**
     * Returns the first update count that matches the
     * specified SQL string. Please note that you can modify
     * the search parameters with {@link #setCaseSensitive} and 
     * {@link #setExactMatch}. Returns <code>null</code> if no
     * return value is present for the specified SQL string.
     * @param sql the SQL string
     * @return the corresponding update count
     */
    public Integer getUpdateCount(String sql)
    {
        List list = SearchUtil.getMatchingObjects(updateCountForStatement, sql, getCaseSensitive(), getExactMatch(), true);
        if(null != list && list.size() > 0)
        {
            return (Integer)list.get(0);
        }
        return null;
    }
    
    /**
     * Returns the global update count for <code>executeUpdate</code>
     * calls.
     * The statement returns the global update count
     * if no update count can be found for the current
     * SQL string.
     * @return the global update count
     */
    public int getGlobalUpdateCount()
    {
        return updateCount;
    }
    
    /**
     * Returns if the specified SQL string is a select that returns
     * a <code>ResultSet</code>.
     * Usually you do not have to specify this.
     * It is assumed that an SQL string returns a <code>ResultSet</code> 
     * if it contains <i>SELECT</i>.
     * @param sql the SQL string
     * @return <code>true</code> if the SQL string returns a <code>ResultSet</code>
     */
    public Boolean getReturnsResultSet(String sql)
    {
        List list = SearchUtil.getMatchingObjects(returnsResultSetMap, sql, getCaseSensitive(), getExactMatch(), true);
        if(null != list && list.size() > 0)
        {
            return (Boolean)list.get(0);
        }
        return null;
    }
    
    /**
     * Prepare a <code>ResultSet</code> for a specified SQL string.
     * @param sql the SQL string
     * @param resultSet the corresponding {@link MockResultSet}
     */
    public void prepareResultSet(String sql, MockResultSet resultSet)
    {
        resultSetsForStatement.put(sql, resultSet);
    }

    /**
     * Prepare the global <code>ResultSet</code>.
     * The statement returns the global <code>ResultSet</code>
     * if no <code>ResultSet</code> can be found for the current
     * SQL string.
     * @param resultSet the {@link MockResultSet}
     */
    public void prepareGlobalResultSet(MockResultSet resultSet)
    {
        this.globalResultSet = resultSet;
    }
    
    /**
     * Prepare the update count for <code>executeUpdate</code> calls 
     * for a specified SQL string.
     * @param sql the SQL string
     * @param updateCount the update count
     */
    public void prepareUpdateCount(String sql, int updateCount)
    {
        updateCountForStatement.put(sql, new Integer(updateCount));
    }
    
    /**
     * Prepare the global update count for <code>executeUpdate</code> calls.
     * The statement returns the global update count
     * if no update count can be found for the current
     * SQL string.
     * @param updateCount the update count
     */
    public void prepareGlobalUpdateCount(int updateCount)
    {
        this.updateCount = updateCount;
    }
    
    /**
     * Prepare if the specified SQL string is a select that returns
     * a <code>ResultSet</code>. Usually you do not have to specify this.
     * It is assumed that an SQL string returns a <code>ResultSet</code> 
     * if it contains <i>SELECT</i>.
     * @param sql the SQL string
     * @param returnsResultSet specify if the SQL string returns a <code>ResultSet</code>
     */
    public void prepareReturnsResultSet(String sql, boolean returnsResultSet)
    {
        returnsResultSetMap.put(sql, new Boolean(returnsResultSet));
    }
    
    /**
     * Clears the global <code>ResultSet</code>.
     */
    public void clearGlobalResultSet()
    {
        this.globalResultSet = null;
    }
    
    /**
     * Returns if specified SQL strings should be handled case sensitive.
     * @return is case sensitivity enabled or disabled
     */
    protected boolean getCaseSensitive()
    {
        return caseSensitive;
    }
    
    /**
     * Returns if specified SQL statements must match exactly.
     * @return is exact matching enabled or disabled
     */
    protected boolean getExactMatch()
    {
        return exactMatch;
    }
}