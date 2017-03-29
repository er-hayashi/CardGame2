package dao.access;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import dao.UserDAO;
import model.UserModel;

@Component
public class UserAccessDAO implements UserDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public List<UserModel> userLogin(UserModel userModel) {
		String sql = "Select * FROM User Where UserId = :UserId And Password = :Password";

		return namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(userModel),
				new BeanPropertyRowMapper<UserModel>(UserModel.class));

	}

	public List<UserModel> userLogin(String userId, String password) {
		String sql = "Select * FROM User Where UserId = :UserId And Password = :Password";

		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("UserId", userId);
		params.addValue("Password", password);

		return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<UserModel>(UserModel.class));

	}

	public List<UserModel> userTestLogin() {

		String sql = "Select u.* from User as u" + " Inner JOIN TestUser as tu ON u.UserId = tu.UserId";

		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserModel>(UserModel.class));
	}

	public void userInsert(UserModel userModel) {

		String sql = "insert into User(UserId, Password, NickName, MailAddress, Birthday, Asset, Status, InsDate, UpdDate, UpdUser)"
				+ " values(:UserId, :Password, :NickName, :MailAddress, :Birthday, :Asset, :Status, :InsDate, :UpdDate, :UpdUser)";

		namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(userModel));
	}

	public void userUpdate(UserModel userModel) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void userDelete(UserModel userModel) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void userSerarch(UserModel userModel) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void updateActive(String userId) {

		String sql = "Update User Set Status = :Status Where UserId = :UserId";
		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("UserId", userId);
		params.addValue("Status", 1);

		namedParameterJdbcTemplate.update(sql, params);
	}

	public void userAssetUpdate(String userId, long prize) {
		String sql = "Update User Set Asset = Asset + (:Prize) Where UserId = :UserId";
		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("UserId", userId);
		params.addValue("Prize", prize);

		namedParameterJdbcTemplate.update(sql, params);
	}

	public void updateUserSessionInformation(HttpSession session) {
		UserModel userModel = (UserModel) session.getAttribute("session");

		UserModel newUserModel = this.getUserInformation(userModel);
		session.setAttribute("session", newUserModel);
	}

	private UserModel getUserInformation(UserModel userModel) {
		String sql = "Select * FROM User Where UserId = :UserId";

		List<UserModel> list = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(userModel),
				new BeanPropertyRowMapper<UserModel>(UserModel.class));

		return list.get(0);
	}

	public List<UserModel> getUserInformation(String userId) {
		String sql = "Select * FROM User Where UserId = :UserId";

		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("UserId", userId);

		return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<UserModel>(UserModel.class));

	}

}