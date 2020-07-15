package cn.edu.ncu.mall.user;

//import cn.edu.ncu.topic.TopicService;
//import cn.edu.ncu.topic.model.Topic;

import cn.edu.ncu.mall.user.model.User;
import cn.edu.ncu.mall.util.TokenUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private final TokenUtil tokenUtil;

    private final UserService userService;


    public UserController(TokenUtil tokenUtil, UserService userService) {
        this.tokenUtil = tokenUtil;
        this.userService = userService;
    }

    /**
     * Registry Api
     * @param request {
     *      "username": username: String[must],
     *      "password": password: String[must],
     *      "name": name: String,
     *      "phone": phone: String,
     *      "address": address: String
     * }
     * @return if registry success return {
     *     "status": 1,
     *     "message": "Registry Success.",
     *     "data": {
     *         "id": user id: BigInteger,
     *         "username": username: String,
     *         "name": name: String
     *     }
     * } else return {
     *     "status: 0,
     *     "message": message: String
     * }
     */
    @ResponseBody
    @PostMapping("/registry")
    public JSONObject registry(@RequestBody JSONObject request) throws MissingServletRequestParameterException {
        JSONObject response = new JSONObject();

        String username = Optional.of(request.getString("username"))
                .orElseThrow(() -> new MissingServletRequestParameterException("username", "String"));
        String password = Optional.of(request.getString("password"))
                .orElseThrow(() -> new MissingServletRequestParameterException("password", "String"));
        String name = request.getString("name");
        String phone = request.getString("phone");
        String address= request.getString("address");

        User user = new User();

        user.setUsername(username);
        user.setName(name);
        user.setPassword(password);
        user.setPhone(phone);
        user.setAddress(address);

        try {
            response.put("data", userService.add(user));
            response.put("status", 1);
            response.put("message", "Registry Success.");
        } catch (DataIntegrityViolationException e) {
            if (userService.checkByUsername(username)) {
                response.put("status", 0);
                response.put("message", "Registry failed: username exits");
            } else {
                throw e;
            }
        }

        return response;
    }

    /**
     * User Login, Get Token Api
     * @param request {
     *      "username": username: String[must],
     *      "password": password: String[must]
     * }
     * @return if login success return {
     *     "status": 1,
     *     "message": "Login success.",
     *     "token": token: String
     * } else return {
     *     "status": 0,
     *     "message": message: String
     * }
     */
    @ResponseBody
    @PostMapping("/token")
    public JSONObject token(@RequestBody JSONObject request) throws MissingServletRequestParameterException {
        JSONObject result = new JSONObject();

        String username = Optional.of(request.getString("username"))
                .orElseThrow(() -> new MissingServletRequestParameterException("username", "String"));

        String password = Optional.of(request.getString("password"))
                .orElseThrow(() -> new MissingServletRequestParameterException("password", "String"));

        try {
            User user = userService.loadUserByUsername(username);
            if (userService.checkPassword(user, password)) {
                result.put("status", 1);
                result.put("message", "Login success.");
                result.put("token", tokenUtil.generateToken(user));
            } else {
                result.put("status", 0);
                result.put("message", "Wrong password.");
            }
        } catch (UsernameNotFoundException e) {
            result.put("status", 0);
            result.put("message", "The user does not exist.");
        }

        return result;
    }
}
