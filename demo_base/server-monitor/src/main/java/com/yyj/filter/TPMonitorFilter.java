package com.yyj.filter;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = {"consumer"})
public class TPMonitorFilter implements Filter {

    /**
     * 总计需要记录近一分钟的数据
     */
    private static final int CALCULATE_SEC_COUNT = 60;

    private final MethodTopPercentile methodTopPercentile =
            MethodTopPercentile.create(CALCULATE_SEC_COUNT, 5);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long startTime = System.currentTimeMillis();

        // 进行处理请求
        Result result = invoker.invoke(invocation);
        // 只有当成功的时候才进行递增数量
        if (result.hasException() || RpcContext.getContext().isAsyncStarted()) {
            return result;
        }

        methodTopPercentile.increment(generateOperateName(invoker.getUrl(), invocation),
                System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * 生成操作名称
     * @param requestURL
     * @param invocation
     * @return
     */
    private String generateOperateName(URL requestURL, Invocation invocation) {

        StringBuilder operateName = new StringBuilder();
        operateName.append(requestURL.getPath());
        operateName.append(".").append(invocation.getMethodName()).append("(");
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                operateName.append(parameterTypes[i].getSimpleName());
                if (i < parameterTypes.length - 1) {
                    operateName.append("").append(",");
                }
            }
        }
        operateName.append(")");
        return operateName.toString();
    }

}
