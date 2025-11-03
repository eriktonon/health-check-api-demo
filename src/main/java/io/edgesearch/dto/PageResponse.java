package io.edgesearch.dto;

import java.util.List;

/**
 * DTO genérico para respostas paginadas.
 *
 * Encapsula informações de paginação junto com os dados retornados.
 *
 * @param <T> tipo dos elementos na página
 * @author Edge Search
 * @version 1.0
 */
public class PageResponse<T> {

    /**
     * Lista de elementos da página atual.
     */
    private List<T> content;

    /**
     * Número da página atual (começando em 0).
     */
    private int page;

    /**
     * Tamanho da página (quantidade de elementos por página).
     */
    private int size;

    /**
     * Total de elementos em todas as páginas.
     */
    private long totalElements;

    /**
     * Total de páginas disponíveis.
     */
    private int totalPages;

    /**
     * Indica se é a primeira página.
     */
    private boolean first;

    /**
     * Indica se é a última página.
     */
    private boolean last;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= totalPages - 1;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
