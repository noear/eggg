package org.noear.eggg.generic;


/**
 * @author noear 2024/10/29 created
 */
public class ServiceImpl<M extends BaseMapper<T>, T> {
    protected M baseMapper;
}