package com.forum.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.forum.dao.ForumDao;
import com.forum.entity.AnswersEntity;
import com.forum.entity.QuestionsEntity;
import com.forum.model.DiscussionModel;

@Repository
public class ForumDaoImpl implements ForumDao {
	@Autowired
	SessionFactory sessionFactory;

	public List<DiscussionModel> searchOnCategory(Integer categoryId) {

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<QuestionsEntity> criteriaQuery = criteriaBuilder.createQuery(QuestionsEntity.class);
		Root<QuestionsEntity> questionsRoot = criteriaQuery.from(QuestionsEntity.class);
		criteriaQuery.select(questionsRoot);
		criteriaQuery.where(criteriaBuilder.equal(questionsRoot.get("questionCategoryId"), categoryId),
				criteriaBuilder.equal(questionsRoot.get("isQuestionActive"), true));
		List<QuestionsEntity> listOfQuestionsWithGivenCategory = session.createQuery(criteriaQuery).getResultList();
		return getListOfDiscussionModelFromQuestionsEntity(listOfQuestionsWithGivenCategory);

	}

	@Override
	public void postQuestions(QuestionsEntity questionEntity) {
		Session session = sessionFactory.getCurrentSession();
		session.save(questionEntity);
	}

	@Override
	public void postAnswers(AnswersEntity answersEntity) {
		Session session = sessionFactory.getCurrentSession();
		session.save(answersEntity);
	}

	public List<DiscussionModel> searchOnKeyword(String searchString) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<QuestionsEntity> criteriaQuery = criteriaBuilder.createQuery(QuestionsEntity.class);
		Root<QuestionsEntity> questionsRoot = criteriaQuery.from(QuestionsEntity.class);
		criteriaQuery.select(questionsRoot);
		EntityType<QuestionsEntity> type = session.getMetamodel().entity(QuestionsEntity.class);
		criteriaQuery.where(
				criteriaBuilder.like(
						criteriaBuilder.lower(questionsRoot
								.get(type.getDeclaredSingularAttribute("questionDescription", String.class))),
						"%" + searchString.toLowerCase() + "%"),
				criteriaBuilder.equal(questionsRoot.get("isQuestionActive"), true));
		List<QuestionsEntity> listOfQuestionsWithGivenSearchString = session.createQuery(criteriaQuery).getResultList();
		return getListOfDiscussionModelFromQuestionsEntity(listOfQuestionsWithGivenSearchString);
	}

	public List<DiscussionModel> searchOnUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<QuestionsEntity> criteriaQuery = criteriaBuilder.createQuery(QuestionsEntity.class);
		Root<QuestionsEntity> questionsRoot = criteriaQuery.from(QuestionsEntity.class);
		criteriaQuery.select(questionsRoot);
		criteriaQuery.where(criteriaBuilder.equal(questionsRoot.get("questionPostedByUserId"), userId),
				criteriaBuilder.equal(questionsRoot.get("isQuestionActive"), true));
		List<QuestionsEntity> listOfQuestionsWithUserId = session.createQuery(criteriaQuery).getResultList();
		return getListOfDiscussionModelFromQuestionsEntity(listOfQuestionsWithUserId);
	}

	@Override
	public QuestionsEntity getQuestionEntityFromQuestionId(int questionId) {
		Session session = sessionFactory.getCurrentSession();
		return getQuestionEntity(questionId, session);
	}

	@Override
	public void editQuestions(int questionId, String question) {
		Session session = sessionFactory.getCurrentSession();
		QuestionsEntity existingQuestionEntity = getQuestionEntity(questionId, session);
		existingQuestionEntity.setQuestionDescription(question);
		session.update(existingQuestionEntity);
	}

	@Override
	public void editAnswers(int answerId, String answer) {
		Session session = sessionFactory.getCurrentSession();
		AnswersEntity existingAnswerEntity = getAnswerEntity(answerId, session);
		existingAnswerEntity.setAnswerDescription(answer);
		session.update(existingAnswerEntity);
	}

	@Override
	public void deleteQuestion(Integer questionId) {
		Session session = sessionFactory.getCurrentSession();
		QuestionsEntity questionEntity = getQuestionEntity(questionId, session);
		questionEntity.setQuestionActive(false);
		session.update(questionEntity);
	}

	@Override
	public void deleteAnswer(Integer answerId) {
		Session session = sessionFactory.getCurrentSession();
		AnswersEntity answersEntity = getAnswerEntity(answerId, session);
		answersEntity.setAnswerIsActive(false);
		session.update(answersEntity);
	}

	@Override
	public void closeQuestions(Integer questionId) {
		Session session = sessionFactory.getCurrentSession();
		QuestionsEntity questionEntity = getQuestionEntity(questionId, session);
		questionEntity.setDiscussionThreadActive(false);
		session.update(questionEntity);
	}

	public List<DiscussionModel> getAnswersByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<AnswersEntity> criteriaQuery = criteriaBuilder.createQuery(AnswersEntity.class);
		Root<AnswersEntity> answersRoot = criteriaQuery.from(AnswersEntity.class);
		criteriaQuery.select(answersRoot);
		criteriaQuery.where(criteriaBuilder.equal(answersRoot.get("answerPostedByUserId"), userId),
				criteriaBuilder.equal(answersRoot.get("answerIsActive"), true));
		List<AnswersEntity> listOfAnswersWithUserId = session.createQuery(criteriaQuery).getResultList();
		return getListOfDiscussionModelFromAnswersEntity(listOfAnswersWithUserId, session);
	}

	private QuestionsEntity getQuestionEntity(Integer questionId, Session session) {
		QuestionsEntity questionEntity = session.get(QuestionsEntity.class, questionId);
		return questionEntity;
	}

	private AnswersEntity getAnswerEntity(Integer answerId, Session session) {
		AnswersEntity answerEntity = session.get(AnswersEntity.class, answerId);
		return answerEntity;
	}

	private List<DiscussionModel> getListOfDiscussionModelFromAnswersEntity(List<AnswersEntity> listOfAnswersWithUserId,
			Session session) {
		List<DiscussionModel> listOfDiscussionModel = new ArrayList<>();
		listOfAnswersWithUserId.forEach(answerEntity -> {
			DiscussionModel discussionModel = new DiscussionModel();
			List<AnswersEntity> listOfAnswers = getListOfAnswersBasedOnAnswerId(answerEntity.getAnswerId());
			discussionModel.setAnswer(listOfAnswers);
			QuestionsEntity questionEntity = session.get(QuestionsEntity.class, answerEntity.getQuestionId());
			discussionModel.setQuestionId(questionEntity.getQuestionId());
			discussionModel.setQuestion(questionEntity.getQuestionDescription());
			discussionModel.setUserId(questionEntity.getQuestionPostedByUserId());
			discussionModel.setDiscussionThreadActive(questionEntity.isDiscussionThreadActive());
			discussionModel.setCategoryId(questionEntity.getQuestionCategoryId());
			discussionModel.setTimestamp(questionEntity.getQuestionCreationDateTime());
			listOfDiscussionModel.add(discussionModel);
		});
		return listOfDiscussionModel;
	}

	private List<DiscussionModel> getListOfDiscussionModelFromQuestionsEntity(
			List<QuestionsEntity> listOfQuestionsWithGivenCategory) {
		List<DiscussionModel> listOfDiscussionModel = new ArrayList<>();

		listOfQuestionsWithGivenCategory.forEach(questionEntity -> {
			DiscussionModel discussionModel = new DiscussionModel();
			discussionModel.setQuestionId(questionEntity.getQuestionId());
			discussionModel.setQuestion(questionEntity.getQuestionDescription());
			discussionModel.setUserId(questionEntity.getQuestionPostedByUserId());
			discussionModel.setDiscussionThreadActive(questionEntity.isDiscussionThreadActive());
			List<AnswersEntity> listOfAnswers = getListOfAnswersBasedOnQuestionId(questionEntity.getQuestionId());
			discussionModel.setAnswer(listOfAnswers);
			discussionModel.setCategoryId(questionEntity.getQuestionCategoryId());
			discussionModel.setTimestamp(questionEntity.getQuestionCreationDateTime());
			listOfDiscussionModel.add(discussionModel);
		});
		return listOfDiscussionModel;
	}

	private List<AnswersEntity> getListOfAnswersBasedOnQuestionId(Integer questionId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<AnswersEntity> criteriaQuery = criteriaBuilder.createQuery(AnswersEntity.class);
		Root<AnswersEntity> questionsRoot = criteriaQuery.from(AnswersEntity.class);
		criteriaQuery.select(questionsRoot);
		criteriaQuery.where(criteriaBuilder.equal(questionsRoot.get("questionId"), questionId),
				criteriaBuilder.equal(questionsRoot.get("answerIsActive"), true));
		return session.createQuery(criteriaQuery).getResultList();
	}

	private List<AnswersEntity> getListOfAnswersBasedOnAnswerId(Integer answerId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<AnswersEntity> criteriaQuery = criteriaBuilder.createQuery(AnswersEntity.class);
		Root<AnswersEntity> answersRoot = criteriaQuery.from(AnswersEntity.class);
		criteriaQuery.select(answersRoot);
		criteriaQuery.where(criteriaBuilder.equal(answersRoot.get("answerId"), answerId),
				criteriaBuilder.equal(answersRoot.get("answerIsActive"), true));
		return session.createQuery(criteriaQuery).getResultList();
	}

}
