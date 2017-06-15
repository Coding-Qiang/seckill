## Dao层设计
1.Dao命名规范：

    实体名+Dao
    一般Dao是针对具体某一实体进行数据操作
    SeckillDao：秒杀商品列表Dao
    SuccessKilledDao：成功秒杀商品Dao
2.围绕增删改查设计接口

	• 增:插入购买明细（SuccessKilledDao）
		○ 通过复合主键过滤重复
	• 改：减库存（SeckillDao）
	• 查：根据id查询单个商品、根据偏移量查询商品列表、根据id查询成功秒杀商品并携带商品实体（多对一）
3.Mybatis特点：
提供参数和SQL（需要自己写SQL语句）

4.SQL写在哪：XML（推荐）和注解

5.接口的实现：

    接口式编程（Mapper自动实现接口）
    API编程方式实现接口（如SqlSession）
6.相关配置文件：mybatis-config.xml、mapper/*.xml

7.mybatis官方文档：http://www.mybatis.org/mybatis-3/zh/index.html

8.mybatis-config.xml的配置：

这里主要配置:

    useGeneratedKeys（自动生成主键）
    mapUnderscoreToCamelCase（驼峰命名映射
    useColumnLabel（使用列标签代替列名，默认为true，可以不配置）
    
    参考http://www.mybatis.org/mybatis-3/zh/configuration.html#settings
    
其他mybatis配置（如声明式事务等）将在与spring整合时spring-dao.xml中在配置

9.mapper

	• 命名规范：Dao名.xml
	• 目的：为Dao接口方法提供sql语句配置
	• 映射方法：namespace = Dao的全限定名、标签id = 接口方法名
	• parameterType、resultType、parameterMap、resultMap
    
10.spring-dao.xml（spring整合mybatis）

	• 配置数据库相关参数，使用properties文件配置键值对，再用context:property-placeholder引入
		○ <context:property-placeholder location="classpath:jdbc.properties" />
	• 配置数据库连接池，<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		○ 配置连接池相关属性（公有属性、私有属性）
			§ 公有属性：数据库相关参数（DriverClass、JdbcUrl、User、Password）
			§ 私有属性：最大连接数和最小连接数、等待获取连接时间、关闭时是否自动提交事务等）
				□ 具体参考：http://blog.csdn.net/caihaijiang/article/details/6843496
	• 配置sqlSesiionFactory对象
		○ 注入数据库连接池（必须有）
		○ 引入mybatis全局配置文件
			§ <property name="configLocation" value="classpath:mybatis-config.xml"/>
				□ 在maven项目中，classpath对应的目录是java和resources
		○ 配置使用别名扫描包
			§ <property name="typeAliasesPackage" value="org.seckill.entity" />，多个包可以用;隔开
		○ 配置扫描mapper映射文件（sql配置文件）
			§ <property name="mapperLocations" value="classpath:mapper/*.xml" />
	• 配置扫描Dao接口包，动态实现Dao接口，并注入到spring容器中
		○ <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
		○ 注入sqlSessionFactory实现了InitializingBean接口，反正提前初始化导致dataSource参数未初始化而失效
        § sqlSessionFactory实现InitializingBean接口的源码
            @Override
            public void afterPropertiesSet() throws Exception {
                notNull(dataSource, "Property 'dataSource' is required");
                notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
                this.sqlSessionFactory = buildSqlSessionFactory();
            }
            
		○ 配置扫描Dao接口包
			§ <property name="basePackage" value="org.seckill.dao"/>
    *特点：
        自动实现Dao接口
        自动注入spring容器
        XML提供sql，Dao接口提供Mapper
 
## Service层设计
1.接口设计原则：站在接口使用者的角度而不是实现的角度设计接口

    方法定义粒度明确
    参数简练
    返回类型友好
    
2.使用DTO（数据传输对象）对返回的与业务不相关的数据进行封装

3.使用异常类定义返回类型

    spring的声明式事务只接收运行期异常回滚策略
    在捕获非运行期异常（编译期异常）时，可以在catch中将其转化为运行期异常并抛出，使spring声明式事务可以做rollback

4.Service的实现

    逻辑的判断
    调用DAO进行数据库操作
    DTO数据的封装
    
5.使用spring工具类digestUtils的工具（如md5DigestAsHex）可以对数据进行加密，如本案例的秒杀地址，可以防止用户提前知道秒杀地址进而使用第三方插件进行秒杀

6.在执行业务操作（秒杀业务）中使用事先定义好的DTO和Exception作为返回类型

7.使用枚举表示常量数据字典

8.使用Spring托管Service：对外提供一致性的访问接口

    对象创建统一托管
    规范的生命周期管理（可以在任意的生命周期点上加入相关逻辑 如init，destroy）
    灵活的依赖注入（注解、编程applicationContext、第三方框架）
    一致的获取对象（默认单例对象）
    
9.IOC注入方式和应用场景：XML、注解、Java配置类

    IOC使用方式：XML配置、package-scan、annotation注解
    
10.spring-service配置

    配置扫描service包下所有使用注解的类型context:component-scan（使用base-package指定包名）
         @Component、@Repository、@Service、@Controller
    配置事务管理器（需要注入数据库连接池）
        mybatis：JDBC事务管理
        hibernate：hibernate事务管理
    配置基于注解的声明式事务 默认使用注解来管理事务行为
        开发团队达成一致的约定，明确标注事务方法的编程风格。
        保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求（如果无法避免，则剥离到事务方法外部）。
        不是所有的方法都需要事务，如只有一条修改操作或只读操作不需要事务控制。（参考行级锁开发文档）
 
## Web层设计
1.Spring MVC路径映射和数据绑定

    使用@RequestMapping做路径映射
    使用@PathVariable将路径中占位符的参数与方法参数做绑定
2.web.xml配置

    配置Spring MVC中央控制器DispatcherServlet（对所有请求进行分发）
		加载整合后的配置文件（连同各个框架的全局配置文件）
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>classpath:spring/spring-*.xml</param-value>
		</init-param>
    配置servlet映射请求，默认匹配所有请求（/）
3.spring-web配置

    开启Spring MVC注解模式
        自动注册DefaultAnnotationHandlerMapping,AnnotationMethodHandlerAdapter
        提供一系列功能：数据绑定，数字和日期的format(@NumberFormat,@DateTimeFormat)，xml、json的默认读写支持
    
    静态资源默认servlet配置（<mvc:default-servlet-handler/>）
        a.加入对静态资源的处理：js,gif,png
        b.允许使用"/"做整体映射
        c.servlet在找页面时，走的是dispatcherServlet路线。找不到的时候会报404，加上这个默认的servlet后，servlet在找不到的时候会去找静态的内容
        d.会把"/**" url,注册到SimpleUrlHandlerMapping的urlMap中,把对静态资源的访问由HandlerMapping转到org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler处理并返回.DefaultServletHttpRequestHandler使用就是各个Servlet容器自己的默认Servlet
    
    配置jsp 显示ViewResolver（InternalResourceViewResolver）
        配置viewClass，方便使用EL表达式和JSTL标签
        配置前缀（prefix）和后缀（suffix），Spring MVC会自动拼接url并转发到相应的视图资源
    配置扫描web相关的bean（<context:component-scan base-package="org.seckill.web"/>）

4.controler的设计

    使用restful风格设计映射路径
    使用model向页面传输数据（addAttribute("key",value)）
    在return中可以使用请求转发或请求重定向（return "redirect:/aa/bb"；return "forward:/aa/bb"）
    使用DTO封装json数据，作为所有ajax请求的返回结果
    对于返回json类型的方法，需要加上@ResponseBody注解，Spring MVC会自动把返回的数据类型封装成json
