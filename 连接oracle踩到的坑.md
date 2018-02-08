### 1. 在 D:\IdeaProjects\traccar\debug.xml 中添加 oracle 连接字符串
    <entry key='database.driver'>oracle.jdbc.OracleDriver</entry>
    <entry key='database.url'>jdbc:oracle:thin:@localhost:1521:GPS</entry>
    <entry key='database.user'>gnss</entry>
    <entry key='database.password'>gnss</entry>
  
### 2. ORA-00923: FROM keyword not found where expected
    将 D:\IdeaProjects\traccar\src\org\traccar\database\DataManager.java 文件中 hikariConfig.setConnectionInitSql(config.getString("database.checkConnection", "SELECT 1")); 修改为 hikariConfig.setConnectionInitSql(config.getString("database.checkConnection", "SELECT 1 from dual"));
    
### 3. ORA-00907: missing right parenthesis
    oracle 的 表别名 不需要 as 关键字，将 D:\IdeaProjects\traccar\schema\changelog-3.7.xml 文件中 <where>groupid NOT IN (SELECT id FROM (SELECT DISTINCT id FROM groups) AS groups_ids)</where> 修改为 <where>groupid NOT IN (SELECT id FROM (SELECT DISTINCT id FROM groups) groups_ids)</where>
   
### 4. ORA-00955: name is already used by an existing object
    将数据库中的表都删除，重新执行一遍建表工作即可，或者在每个建表语句前先做一下是否有表的判断
  
### 5. ORA-00910: specified length too long for its datatype
    将 D:\IdeaProjects\traccar\schema\ 文件夹下的 文件 中的 字段大小 设置为 小于 4000 或者 小于 2000 均可
    

    
    
    
    
    
    
    
    
    
    
    
    