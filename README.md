# 这是 MyBatis Generator 插件的拓展插件包的拓展插件包  
感谢[itfsw/mybatis-generator-plugin](https://github.com/itfsw/mybatis-generator-plugin)项目为mybatis的代码自动生成作出了非常卓越的贡献, 鉴于其提供的代码生成方法仍然不能完全满足我的业务需求, 我建立了这个分支, 旨在根据我的需求增加代码生成的策略, 原策略的使用参照[https://github.com/itfsw/mybatis-generator-plugin](https://github.com/itfsw/mybatis-generator-plugin), 我在此说明由我增加的新的策略  

- 实体字段约束注解插件
- 实体字段注释插件
- 列辅助插件
- 基于任意列名的条件查询插件
- 基于Map的组合条件查询插件
- 基于Model的组合条件查询插件
- 基于Filter结构的组合条件查询插件
- toString方法生成插件

# 插件使用说明
## 实体字段约束注解插件 (AutoTableFieldAnnotationPlugin)
通过读取数据库的字段约束来为生成的实体字段生成相应的注解, 支持的注解包括
- `@Min`
- `@Max`
- `@Null`
- `@NotNull`
- `@NotEmpty`

*`NotNull`和`NotEmpty`的区别在于其对应的字段是否为字符串, 在大多数情况下, 空字符串是没有意义的*

具体效果如下:
```java
public class Company {
    @Min(1)
    @Max(20)
    private Long companyId;

    @NotBlank
    @Min(1)
    @Max(200)
    private String companyName;

    @Min(0)
    @Max(1)
    private String recordState;

    @NotNull
    @Min(1)
    @Max(19)
    private Long creatorId;

    @NotNull
    private Date createTime;
}
```

## 实体字段注释插件 (AutoTableFieldCommentPlugin)
## 列辅助插件 (ExampleColumnHelperPlugin)
插件为辅助其他插件可简单地判断传入字段是否为合法且存在的字段以及传入JavaBean类字段名时获取对应的数据库字段名.  
具体生成的方法如下:
```java
public static final String[][] FIELD_NAME_LIST = new String[][]{
        {"company_id", "companyId"}, 
        {"company_name", "companyName"}, 
        {"address", "address"}, 
        {"telephone", "telephone"}, 
        {"website", "website"}, 
        {"record_state", "recordState"}, 
        {"creator_id", "creatorId"}, 
        {"create_time", "createTime"}, 
        {"modifier_id", "modifierId"}, 
        {"modify_time", "modifyTime"}};

private static boolean existsColumn(String field) {
    return Arrays.stream(FIELD_NAME_LIST).anyMatch(strings -> strings[0].equalsIgnoreCase(field) || strings[0].equalsIgnoreCase(field));
}

private static String getActualColumnName(String field) {
    return Arrays.stream(FIELD_NAME_LIST).filter(strings -> strings[0].equalsIgnoreCase(field) || strings[1].equalsIgnoreCase(field)).findFirst().map(strings -> strings[0]).orElse("");
}
```

## 基于任意列名的条件查询插件 (ExampleFieldEnhancePlugin)
在基于REST接口进行交互的前后端分离架构中, 前端是无法操作Java方法的, 在前端需要进行不确定字段的查询的时候, 需要通过传入字段名来构造查询sql, 故使用此插件.  
具体效果如下:
```java
@Service
public class CompanyServiceImpl {
    // 代码里展示的是根据范围和查询字段来确定符合条件的总条数
    @Override
    public Long count(String field, String start, String end) {
        return companyMapper.countByExample(
                new CompanyQuery()
                        .or()
                        .andFieldBetween(field, start, end)
                        .example());
    }
}
```

## 基于Map的组合条件查询插件 (ExampleMapEnhancedPlugin)
在基于REST接口进行交互的前后端分离架构中, 前端构造Map远比构造Url参数要简单得多, 所以在多条件组合查询的时候, 直接向后端传一个Map结构来表达组合查询条件是非常方便的.  
具体效果如下:  
*查询参数*
```json
{
  "companyName": "testCompanyName",
  "createTime": "2019-05-06"
}
```
*在Example中构造条件, 支持`EqualTo, NotEqualTo, Like, NotLike, GreaterThan, GreaterThanOrEqualTo, LessThan, LessThanOrEqualTo`*
```java
@Override
public List<Company> queryActive(Map<String, Object> company) {
    return companyMapper.selectByExample(
            new CompanyQuery()
                    .or()
                    .andMapEqualTo(company)
                    .example());
}
```

## 基于Model的组合条件查询插件 (ExampleModelEnhancedPlugin)
同*ExampleMapEnhancedPlugin*, 使用方法相同, 唯一的区别是传入的参数为实体.  

## 基于Filter结构的组合条件查询插件 (ExampleFilterEnhancedPlugin)
## `toString`方法生成插件 (ToStringPlugin)
生成不同风格的`toString`方法.  