package org.centralperf.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
public class Script{

	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Label of the script
	 */
	private String label;

    /**
     * Short description for the script
     */
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    private Project project;

    @OneToMany(cascade=CascadeType.ALL)
    @OrderColumn(name="INDEX")
    private List<ScriptVersion> versions = new ArrayList<ScriptVersion>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ScriptVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ScriptVersion> versions) {
        this.versions = versions;
    }
}