package net.csdn.jpa.model;

import net.csdn.common.param.ParamBinding;
import net.csdn.jpa.JPA;
import net.csdn.jpa.context.JPAConfig;
import net.csdn.jpa.context.JPAContext;
import net.csdn.validate.ValidateParse;
import net.csdn.validate.ValidateResult;
import net.csdn.validate.impl.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;

import javax.persistence.EntityManager;
import javax.persistence.Transient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.csdn.common.collections.WowCollections.newArrayList;

/**
 * User: WilliamZhu
 * Date: 12-6-26
 * Time: 下午9:53
 */
public class JPABase implements Model {

    public final static List validateParses = newArrayList();

    public static JPAContext getJPAContext() {
        return getJPAConfig().getJPAContext();
    }


    public static JPAConfig getJPAConfig() {
        return JPA.getJPAConfig();
    }

    //强类型 没办法呀
    public <T> T attr(String fieldName, Class<T> clzz) {
        try {
            Field field = (this.getClass().getDeclaredField(fieldName));
            field.setAccessible(true);
            return clzz.cast(field.get(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public JPABase attr(String fieldName, Object value) {
        try {
            BeanUtils.setProperty(this, fieldName, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public <T extends JPABase> T m(String methodName, Object... objs) {

        try {
            return (T) MethodUtils.invokeMethod(this, methodName, objs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends Model> T save() {
        em().persist(this);
        em().flush();
        return (T) this;
    }

    public <T extends JPABase> T add(Map params) {
        ParamBinding paramBinding = new ParamBinding();
        paramBinding.parse(params);
        paramBinding.toModel(this);
        return (T) this;
    }

    @Transient
    public final List<ValidateResult> validateResults = new ArrayList<ValidateResult>();

    public boolean valid() {
        for (Object validateParse : validateParses) {
            ((ValidateParse) validateParse).parse(this, this.validateResults);
        }
        if (validateResults.size() > 0) return false;
        return true;
    }

    public EntityManager em() {
        return getJPAContext().em();
    }

    @Override
    public void delete() {
        em().remove(this);
        em().flush();
    }

    @Override
    public void update() {
        em().refresh(this);
        em().flush();
    }

    @Override
    public Object key() {
        return null;
    }

}
