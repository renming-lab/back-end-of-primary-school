package summer.project.shiro;

import cn.hutool.core.bean.BeanUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import summer.project.entity.TestUser;
import summer.project.entity.User;
import summer.project.service.TestUserService;
import summer.project.service.UserService;
import summer.project.util.JwtUtils;
import summer.project.util.ShiroUtil;

@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    // 必须支持JwtToken
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("授权");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        AccountProfile principal = ShiroUtil.getProfile();

        info.addStringPermission(principal.getPerms());

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("认证");
//        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
//        TestUser user = userService.queryUserByName(token.getUsername());
//        if (user == null) {
//            // 报出Unknown...异常
//            return null;
//        }
//        return new SimpleAuthenticationInfo("", user.getPassword(), "");
//        return null;


        JwtToken jwtToken = (JwtToken) authenticationToken;
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();
        User user = userService.getById(Long.parseLong(userId));
        if (user == null) {
            throw new UnknownAccountException("账户不存在");
        }
//        if (user.getStatus() == -1) {
//            throw new LockedAccountException("账户已被锁定");
//        }
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user, profile);
        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());
    }


}