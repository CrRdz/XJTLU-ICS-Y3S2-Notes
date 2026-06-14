package com.cpt202.repository;

import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.vo.RequestStatusHistoryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {

    List<RequestStatusHistory> findByRequest_RequestIdOrderByChangedAtAsc(Long requestId);

    List<RequestStatusHistory> findAllByOrderByChangedAtAsc();

    @Query("""
            select new com.cpt202.vo.RequestStatusHistoryVO(
                h.historyId,
                r.requestId,
                h.oldStatus,
                h.newStatus,
                s.studentId,
                su.fullName,
                h.remark,
                h.changedAt
            )
            from RequestStatusHistory h
            left join h.request r
            left join h.changedBy s
            left join s.user su
            order by h.changedAt asc
            """)
    List<RequestStatusHistoryVO> findAllHistoryVos();

    @Query(value = """
            select new com.cpt202.vo.RequestStatusHistoryVO(
                h.historyId,
                r.requestId,
                h.oldStatus,
                h.newStatus,
                s.studentId,
                su.fullName,
                h.remark,
                h.changedAt
            )
            from RequestStatusHistory h
            left join h.request r
            left join h.changedBy s
            left join s.user su
            order by h.changedAt asc
            """,
            countQuery = "select count(h) from RequestStatusHistory h")
    Page<RequestStatusHistoryVO> findAllHistoryVos(Pageable pageable);
}
