package com.elearning.elearning_api.config;

import com.elearning.elearning_api.entity.AvisCours;
import com.elearning.elearning_api.entity.Categorie;
import com.elearning.elearning_api.entity.Certificat;
import com.elearning.elearning_api.entity.Choix;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Formateur;
import com.elearning.elearning_api.entity.Inscription;
import com.elearning.elearning_api.entity.Lecon;
import com.elearning.elearning_api.entity.PaiementCours;
import com.elearning.elearning_api.entity.ProgressionLecon;
import com.elearning.elearning_api.entity.Question;
import com.elearning.elearning_api.entity.ResultatEvaluation;
import com.elearning.elearning_api.entity.SousCategorie;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.enums.EtatCours;
import com.elearning.elearning_api.enums.Role;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.enums.StatutPaiement;
import com.elearning.elearning_api.enums.TypeEvaluation;
import com.elearning.elearning_api.repository.AvisCoursRepository;
import com.elearning.elearning_api.repository.CategorieRepository;
import com.elearning.elearning_api.repository.CertificatRepository;
import com.elearning.elearning_api.repository.ChoixRepository;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.FormateurRepository;
import com.elearning.elearning_api.repository.InscriptionRepository;
import com.elearning.elearning_api.repository.LeconRepository;
import com.elearning.elearning_api.repository.PaiementCoursRepository;
import com.elearning.elearning_api.repository.ProgressionLeconRepository;
import com.elearning.elearning_api.repository.QuestionRepository;
import com.elearning.elearning_api.repository.ResultatEvaluationRepository;
import com.elearning.elearning_api.repository.SousCategorieRepository;
import com.elearning.elearning_api.repository.UtilisateurRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UtilisateurRepository utilisateurRepository,
                           FormateurRepository formateurRepository,
                           EtudiantRepository etudiantRepository,
                           CategorieRepository categorieRepository,
                           SousCategorieRepository sousCategorieRepository,
                           CoursRepository coursRepository,
                           LeconRepository leconRepository,
                           EvaluationRepository evaluationRepository,
                           QuestionRepository questionRepository,
                           ChoixRepository choixRepository,
                           InscriptionRepository inscriptionRepository,
                           AvisCoursRepository avisCoursRepository,
                           PaiementCoursRepository paiementCoursRepository,
                           ProgressionLeconRepository progressionLeconRepository,
                           ResultatEvaluationRepository resultatEvaluationRepository,
                           CertificatRepository certificatRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            createAdminIfMissing(utilisateurRepository, passwordEncoder);

            DemoUsers demoUsers = createDemoUsers(
                    utilisateurRepository,
                    formateurRepository,
                    etudiantRepository,
                    passwordEncoder
            );

            DemoCatalog demoCatalog = createDemoCatalog(
                    categorieRepository,
                    sousCategorieRepository,
                    coursRepository,
                    demoUsers
            );

            DemoLearning demoLearning = createDemoLearning(
                    leconRepository,
                    evaluationRepository,
                    questionRepository,
                    choixRepository,
                    demoCatalog
            );

            DemoAcademy demoAcademy = createDemoAcademy(
                    inscriptionRepository,
                    demoUsers,
                    demoCatalog
            );

            createDemoReviews(avisCoursRepository, demoUsers, demoCatalog);
            createDemoPayments(paiementCoursRepository, demoAcademy, demoCatalog);
            createDemoProgressions(progressionLeconRepository, demoUsers, demoLearning);
            createDemoResults(resultatEvaluationRepository, demoUsers, demoLearning);
            createDemoCertificates(certificatRepository, demoUsers, demoCatalog);
        };
    }

    private void createAdminIfMissing(UtilisateurRepository utilisateurRepository,
                                      PasswordEncoder passwordEncoder) {
        if (utilisateurRepository.findByEmail("admin@gmail.com").isPresent()) {
            return;
        }

        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin EduNet");
        admin.setEmail("admin@gmail.com");
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setStatus("ACTIVE");
        utilisateurRepository.save(admin);
    }

    private DemoUsers createDemoUsers(UtilisateurRepository utilisateurRepository,
                                      FormateurRepository formateurRepository,
                                      EtudiantRepository etudiantRepository,
                                      PasswordEncoder passwordEncoder) {
        Map<String, Formateur> teachers = new LinkedHashMap<>();

        teachers.put("sami.formateur@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Sami Ben Amor",
                "sami.formateur@gmail.com",
                "teacher123",
                "ACTIVE",
                "Angular, Spring Boot et architecture full stack",
                "https://portfolio.edunet.local/sami",
                "Lead formateur sur les parcours web et full stack.",
                "Partager des parcours concrets relies a de vraies applications."
        ));

        teachers.put("karim.data@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Karim Data",
                "karim.data@gmail.com",
                "teacher123",
                "ACTIVE",
                "SQL, BI et machine learning pour metiers data",
                "https://portfolio.edunet.local/karim",
                "Formateur data oriente dashboards, KPIs et premiers pipelines ML.",
                "Aider les apprenants a lire, nettoyer et raconter les donnees."
        ));

        teachers.put("leila.design@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Leila Design",
                "leila.design@gmail.com",
                "teacher123",
                "ACTIVE",
                "UX research, UI design et design systems",
                "https://portfolio.edunet.local/leila",
                "Designer produit focalisee sur la recherche et les interfaces premium.",
                "Faire monter en niveau les profils design et produit."
        ));

        teachers.put("sara.growth@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Sara Growth",
                "sara.growth@gmail.com",
                "teacher123",
                "ACTIVE",
                "SEO, social media et acquisition digitale",
                "https://portfolio.edunet.local/sara",
                "Formatrice marketing digital pour les parcours acquisition et contenu.",
                "Rendre le marketing digital plus concret et mesurable."
        ));

        teachers.put("mehdi.coach@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Mehdi Coach",
                "mehdi.coach@gmail.com",
                "teacher123",
                "ACTIVE",
                "Gestion de projet, communication et collaboration",
                "https://portfolio.edunet.local/mehdi",
                "Coach projets pour les sujets agile, organisation et communication pro.",
                "Aider les apprenants a mieux collaborer et mieux presenter."
        ));

        teachers.put("walid.secure@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Walid Secure",
                "walid.secure@gmail.com",
                "teacher123",
                "ACTIVE",
                "Cybersecurite web, auth et hygiene numerique",
                "https://portfolio.edunet.local/walid",
                "Formateur securite avec une approche tres operationnelle.",
                "Installer des reflexes securite utiles dans les projets reels."
        ));

        teachers.put("ines.career@gmail.com", findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Ines Career",
                "ines.career@gmail.com",
                "teacher123",
                "ACTIVE",
                "Anglais professionnel, employabilite et communication client",
                "https://portfolio.edunet.local/ines",
                "Formatrice orientee communication professionnelle et carriere.",
                "Donner des outils concrets pour mieux parler, ecrire et convaincre."
        ));

        Formateur pendingTeacher = findOrCreateTeacher(
                utilisateurRepository, formateurRepository, passwordEncoder,
                "Nour Candidate",
                "nour.formateur@gmail.com",
                "teacher123",
                "EN_ATTENTE",
                "UI UX et contenu visuel",
                "https://portfolio.edunet.local/nour",
                "Candidate en attente de validation pour la file admin.",
                "Animer des parcours design et contenus visuels pour la plateforme."
        );

        Map<String, Etudiant> students = new LinkedHashMap<>();
        students.put("emna@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Emna Ben Ali", "emna@gmail.com", "student123"
        ));
        students.put("aya.etudiante@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Aya Etudiante", "aya.etudiante@gmail.com", "student123"
        ));
        students.put("yassine.product@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Yassine Product", "yassine.product@gmail.com", "student123"
        ));
        students.put("meriem.growth@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Meriem Growth", "meriem.growth@gmail.com", "student123"
        ));
        students.put("walid.student@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Walid Etudiant", "walid.student@gmail.com", "student123"
        ));
        students.put("lina.career@gmail.com", findOrCreateStudent(
                utilisateurRepository, etudiantRepository, passwordEncoder,
                "Lina Career", "lina.career@gmail.com", "student123"
        ));

        return new DemoUsers(teachers, students, pendingTeacher);
    }

    private DemoCatalog createDemoCatalog(CategorieRepository categorieRepository,
                                          SousCategorieRepository sousCategorieRepository,
                                          CoursRepository coursRepository,
                                          DemoUsers demoUsers) {
        Categorie dev = findOrCreateCategory(
                categorieRepository,
                "Developpement Web",
                "Frontend, backend, full stack et architecture d applications web."
        );
        Categorie data = findOrCreateCategory(
                categorieRepository,
                "Data & IA",
                "Analyse de donnees, reporting metier, dashboards et premiers modeles ML."
        );
        Categorie design = findOrCreateCategory(
                categorieRepository,
                "Design Produit",
                "UX research, UI design, Figma et design systems."
        );
        Categorie marketing = findOrCreateCategory(
                categorieRepository,
                "Marketing Digital",
                "SEO, contenu, reseaux sociaux et acquisition."
        );
        Categorie business = findOrCreateCategory(
                categorieRepository,
                "Business & Productivite",
                "Gestion de projet, communication et outils de collaboration."
        );
        Categorie cyber = findOrCreateCategory(
                categorieRepository,
                "Cybersecurite",
                "Bonnes pratiques, securite web et hygiene numerique."
        );
        Categorie career = findOrCreateCategory(
                categorieRepository,
                "Langues & Carriere",
                "Anglais professionnel, employabilite et communication client."
        );
        Categorie cloud = findOrCreateCategory(
                categorieRepository,
                "Cloud & DevOps",
                "Docker, CI CD, orchestration et environnements de livraison."
        );

        Map<String, SousCategorie> subCategories = new LinkedHashMap<>();
        subCategories.put("Angular", findOrCreateSubCategory(
                sousCategorieRepository, "Angular",
                "Applications Angular modernes et maintainables.", dev
        ));
        subCategories.put("Spring Boot", findOrCreateSubCategory(
                sousCategorieRepository, "Spring Boot",
                "APIs REST, securite et persistence avec Spring Boot.", dev
        ));
        subCategories.put("Full Stack", findOrCreateSubCategory(
                sousCategorieRepository, "Full Stack",
                "Parcours relies entre frontend Angular et backend Spring Boot.", dev
        ));
        subCategories.put("Analyse de donnees", findOrCreateSubCategory(
                sousCategorieRepository, "Analyse de donnees",
                "SQL, KPIs et tableaux de bord pour mieux piloter.", data
        ));
        subCategories.put("BI & Reporting", findOrCreateSubCategory(
                sousCategorieRepository, "BI & Reporting",
                "Reporting metier, dataviz et tableaux de bord executifs.", data
        ));
        subCategories.put("Machine Learning", findOrCreateSubCategory(
                sousCategorieRepository, "Machine Learning",
                "Concepts ML, evaluation et premiers pipelines data.", data
        ));
        subCategories.put("UX Research", findOrCreateSubCategory(
                sousCategorieRepository, "UX Research",
                "Interviews, syntheses et hypotheses produit.", design
        ));
        subCategories.put("UI Design", findOrCreateSubCategory(
                sousCategorieRepository, "UI Design",
                "Interfaces premium, Figma et design systems.", design
        ));
        subCategories.put("SEO & Content", findOrCreateSubCategory(
                sousCategorieRepository, "SEO & Content",
                "Visibilite organique, mots cles et strategie de contenu.", marketing
        ));
        subCategories.put("Social Media", findOrCreateSubCategory(
                sousCategorieRepository, "Social Media",
                "Animation de marque, publications et campagnes.", marketing
        ));
        subCategories.put("Gestion de projet", findOrCreateSubCategory(
                sousCategorieRepository, "Gestion de projet",
                "Agile, priorisation et pilotage de livrables.", business
        ));
        subCategories.put("Communication Pro", findOrCreateSubCategory(
                sousCategorieRepository, "Communication Pro",
                "Presentations, ecriture pro et communication interne.", business
        ));
        subCategories.put("Securite Web", findOrCreateSubCategory(
                sousCategorieRepository, "Securite Web",
                "OWASP, auth, JWT et permissions applicatives.", cyber
        ));
        subCategories.put("Hygiene Numerique", findOrCreateSubCategory(
                sousCategorieRepository, "Hygiene Numerique",
                "Reflexes de securite utiles au quotidien.", cyber
        ));
        subCategories.put("Anglais Professionnel", findOrCreateSubCategory(
                sousCategorieRepository, "Anglais Professionnel",
                "Reunions, emails et echanges client en anglais.", career
        ));
        subCategories.put("Employabilite", findOrCreateSubCategory(
                sousCategorieRepository, "Employabilite",
                "Preparation d entretien, CV et communication de carriere.", career
        ));
        subCategories.put("Docker & CI/CD", findOrCreateSubCategory(
                sousCategorieRepository, "Docker & CI/CD",
                "Conteneurisation et automatisation de livraison.", cloud
        ));
        subCategories.put("Kubernetes", findOrCreateSubCategory(
                sousCategorieRepository, "Kubernetes",
                "Orchestration et gestion d applications distribuees.", cloud
        ));

        List<CourseSeed> courseSeeds = List.of(
                new CourseSeed(
                        "Angular 21 de zero a projet reel",
                        "Construis une interface moderne, connectee a une API Spring Boot, avec routing, auth et composants reutilisables.",
                        "sami.formateur@gmail.com", "Angular", "12h", "debutant",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80",
                        amount("59.00"), EtatCours.PUBLIE, 4
                ),
                new CourseSeed(
                        "Spring Boot API securisee",
                        "Mets en place une API REST avec JWT, JPA et validation pour une vraie application e-learning.",
                        "sami.formateur@gmail.com", "Spring Boot", "10h", "intermediaire",
                        "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80",
                        amount("79.00"), EtatCours.PUBLIE, 3
                ),
                new CourseSeed(
                        "Angular & Spring Boot full stack",
                        "Relie un frontend Angular moderne a une API Spring Boot avec auth, catalogue et dashboards.",
                        "sami.formateur@gmail.com", "Full Stack", "14h", "intermediaire",
                        "https://images.unsplash.com/photo-1516321165247-4aa89a48be28?auto=format&fit=crop&w=1200&q=80",
                        amount("99.00"), EtatCours.PUBLIE, 2
                ),
                new CourseSeed(
                        "Docker & CI pour applications web",
                        "Conteneurise ton application, prepare tes pipelines et automatise la livraison d un produit web.",
                        "sami.formateur@gmail.com", "Docker & CI/CD", "7h", "intermediaire",
                        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1200&q=80",
                        amount("69.00"), EtatCours.PUBLIE, 5
                ),
                new CourseSeed(
                        "SQL & dashboards pour debutants",
                        "Apprends a manipuler des donnees, construire des indicateurs et alimenter des tableaux de bord utiles.",
                        "karim.data@gmail.com", "Analyse de donnees", "8h", "debutant",
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80",
                        amount("39.00"), EtatCours.PUBLIE, 6
                ),
                new CourseSeed(
                        "Power BI & reporting metier",
                        "Transforme des donnees en tableaux de bord clairs pour piloter les ventes, le marketing et les operations.",
                        "karim.data@gmail.com", "BI & Reporting", "6h", "intermediaire",
                        "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80",
                        amount("49.00"), EtatCours.PUBLIE, 2
                ),
                new CourseSeed(
                        "Machine learning pour debutants",
                        "Decouvre les concepts essentiels du machine learning et entraine tes premiers modeles.",
                        "karim.data@gmail.com", "Machine Learning", "9h", "intermediaire",
                        "https://images.unsplash.com/photo-1526378722484-bd91ca387e72?auto=format&fit=crop&w=1200&q=80",
                        amount("89.00"), EtatCours.PUBLIE, 1
                ),
                new CourseSeed(
                        "UX research appliquee au produit",
                        "Structure tes interviews, syntheses et hypotheses pour faire evoluer un produit de maniere fiable.",
                        "leila.design@gmail.com", "UX Research", "6h", "debutant",
                        "https://images.unsplash.com/photo-1545235617-9465d2a55698?auto=format&fit=crop&w=1200&q=80",
                        amount("45.00"), EtatCours.PUBLIE, 5
                ),
                new CourseSeed(
                        "Design system pour applications web",
                        "Construis une bibliotheque de composants reutilisables et un langage visuel coherent.",
                        "leila.design@gmail.com", "UI Design", "7h", "intermediaire",
                        "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80",
                        amount("65.00"), EtatCours.PUBLIE, 4
                ),
                new CourseSeed(
                        "Figma to code pour equipes produit",
                        "Passe d une maquette Figma a une interface integree proprement dans un projet Angular.",
                        "leila.design@gmail.com", "UI Design", "5h", "intermediaire",
                        "https://images.unsplash.com/photo-1522542550221-31fd19575a2d?auto=format&fit=crop&w=1200&q=80",
                        amount("55.00"), EtatCours.PUBLIE, 3
                ),
                new CourseSeed(
                        "SEO pratique pour lancer un site",
                        "Travaille les mots cles, les pages, le contenu et la structure technique pour gagner en visibilite.",
                        "sara.growth@gmail.com", "SEO & Content", "5h", "debutant",
                        "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=1200&q=80",
                        amount("35.00"), EtatCours.PUBLIE, 6
                ),
                new CourseSeed(
                        "Social media manager starter",
                        "Planifie des publications, mesure les performances et anime une marque sur les reseaux.",
                        "sara.growth@gmail.com", "Social Media", "4h", "debutant",
                        "https://images.unsplash.com/photo-1611162617474-5b21e879e113?auto=format&fit=crop&w=1200&q=80",
                        amount("29.00"), EtatCours.PUBLIE, 4
                ),
                new CourseSeed(
                        "Growth marketing & analytics",
                        "Monte des experiments simples et suis les bons indicateurs pour comprendre la croissance.",
                        "sara.growth@gmail.com", "SEO & Content", "6h", "intermediaire",
                        "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80",
                        amount("59.00"), EtatCours.PUBLIE, 2
                ),
                new CourseSeed(
                        "Gestion de projet agile pour juniors",
                        "Apprends a cadrer un projet, organiser les taches et suivre les livrables avec une equipe.",
                        "mehdi.coach@gmail.com", "Gestion de projet", "6h", "debutant",
                        "https://images.unsplash.com/photo-1517048676732-d65bc937f952?auto=format&fit=crop&w=1200&q=80",
                        amount("42.00"), EtatCours.PUBLIE, 5
                ),
                new CourseSeed(
                        "Communication professionnelle & presentations",
                        "Ameliore tes presentations, tes emails et ta communication dans un contexte de travail.",
                        "mehdi.coach@gmail.com", "Communication Pro", "3h30", "debutant",
                        "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=1200&q=80",
                        amount("25.00"), EtatCours.PUBLIE, 3
                ),
                new CourseSeed(
                        "Cyber hygiene au quotidien",
                        "Mets en place les bons reflexes de securite pour proteger comptes, appareils et donnees.",
                        "walid.secure@gmail.com", "Hygiene Numerique", "2h30", "debutant",
                        "https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1200&q=80",
                        amount("0.00"), EtatCours.PUBLIE, 7
                ),
                new CourseSeed(
                        "Securiser une application web moderne",
                        "Passe en revue auth, JWT, permissions, OWASP et hygiene de code pour une application web complete.",
                        "walid.secure@gmail.com", "Securite Web", "8h", "intermediaire",
                        "https://images.unsplash.com/photo-1510511459019-5dda7724fd87?auto=format&fit=crop&w=1200&q=80",
                        amount("75.00"), EtatCours.PUBLIE, 2
                ),
                new CourseSeed(
                        "Anglais professionnel pour freelances",
                        "Travaille ton anglais pour les rendez vous client, les presentations et les echanges ecrits.",
                        "ines.career@gmail.com", "Anglais Professionnel", "5h30", "debutant",
                        "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80",
                        amount("32.00"), EtatCours.PUBLIE, 4
                ),
                new CourseSeed(
                        "Kubernetes pour equipes web",
                        "Comprends les bases du deploiement orchestre pour des applications web plus robustes.",
                        "sami.formateur@gmail.com", "Kubernetes", "5h", "avance",
                        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1200&q=80",
                        amount("85.00"), EtatCours.EN_ATTENTE_VALIDATION, 0
                ),
                new CourseSeed(
                        "Preparer un entretien technique en anglais",
                        "Travaille ton vocabulaire, ta posture et tes reponses pour reussir un entretien client ou technique.",
                        "ines.career@gmail.com", "Employabilite", "4h", "intermediaire",
                        "https://images.unsplash.com/photo-1521791136064-7986c2920216?auto=format&fit=crop&w=1200&q=80",
                        amount("28.00"), EtatCours.BROUILLON, 0
                )
        );

        Map<String, Cours> courses = new LinkedHashMap<>();
        for (CourseSeed seed : courseSeeds) {
            Formateur teacher = demoUsers.teacher(seed.teacherEmail());
            SousCategorie subCategory = subCategories.get(seed.subCategoryName());
            if (subCategory == null) {
                throw new IllegalStateException("Sous-categorie introuvable: " + seed.subCategoryName());
            }
            Cours course = findOrCreateCourse(coursRepository, seed, teacher, subCategory);
            courses.put(seed.title(), course);
        }

        return new DemoCatalog(courses);
    }

    private DemoLearning createDemoLearning(LeconRepository leconRepository,
                                            EvaluationRepository evaluationRepository,
                                            QuestionRepository questionRepository,
                                            ChoixRepository choixRepository,
                                            DemoCatalog demoCatalog) {
        Map<String, List<Lecon>> lessonsByCourse = new LinkedHashMap<>();
        Map<String, Evaluation> quizzesByCourse = new LinkedHashMap<>();

        for (Cours course : demoCatalog.courses().values()) {
            List<Lecon> lessons = new ArrayList<>();
            lessons.add(findOrCreateLesson(
                    leconRepository,
                    course,
                    1,
                    "Bien demarrer le parcours",
                    "Comprendre la promesse du cours, son niveau et les resultats attendus."
            ));
            lessons.add(findOrCreateLesson(
                    leconRepository,
                    course,
                    2,
                    "Atelier guide",
                    "Mettre en pratique les notions du cours a travers un cas concret accompagne."
            ));
            lessons.add(findOrCreateLesson(
                    leconRepository,
                    course,
                    3,
                    "Passage a la pratique",
                    "Consolider les acquis et preparer la suite du parcours avec des actions claires."
            ));

            lessonsByCourse.put(course.getTitre(), lessons);

            Evaluation quiz = createQuizWithQuestion(
                    evaluationRepository,
                    questionRepository,
                    choixRepository,
                    lessons.get(1),
                    "Quiz - " + course.getTitre(),
                    "Quel est le meilleur objectif du module \"" + lessons.get(1).getTitre() + "\" ?",
                    "Mettre en pratique les notions du cours dans un cas concret",
                    List.of(
                            "Ignorer les besoins des apprenants",
                            "Supprimer les validations et controles"
                    )
            );
            quizzesByCourse.put(course.getTitre(), quiz);
        }

        return new DemoLearning(lessonsByCourse, quizzesByCourse);
    }

    private DemoAcademy createDemoAcademy(InscriptionRepository inscriptionRepository,
                                          DemoUsers demoUsers,
                                          DemoCatalog demoCatalog) {
        List<EnrollmentSeed> enrollmentSeeds = List.of(
                new EnrollmentSeed("emna@gmail.com", "Angular 21 de zero a projet reel", StatutInscription.VALIDE),
                new EnrollmentSeed("emna@gmail.com", "Spring Boot API securisee", StatutInscription.VALIDE),
                new EnrollmentSeed("emna@gmail.com", "SQL & dashboards pour debutants", StatutInscription.VALIDE),
                new EnrollmentSeed("emna@gmail.com", "Angular & Spring Boot full stack", StatutInscription.EN_ATTENTE),
                new EnrollmentSeed("aya.etudiante@gmail.com", "Angular 21 de zero a projet reel", StatutInscription.VALIDE),
                new EnrollmentSeed("aya.etudiante@gmail.com", "UX research appliquee au produit", StatutInscription.VALIDE),
                new EnrollmentSeed("aya.etudiante@gmail.com", "Design system pour applications web", StatutInscription.VALIDE),
                new EnrollmentSeed("aya.etudiante@gmail.com", "Figma to code pour equipes produit", StatutInscription.VALIDE),
                new EnrollmentSeed("yassine.product@gmail.com", "Gestion de projet agile pour juniors", StatutInscription.VALIDE),
                new EnrollmentSeed("yassine.product@gmail.com", "Communication professionnelle & presentations", StatutInscription.VALIDE),
                new EnrollmentSeed("yassine.product@gmail.com", "Angular & Spring Boot full stack", StatutInscription.REFUSE),
                new EnrollmentSeed("meriem.growth@gmail.com", "SQL & dashboards pour debutants", StatutInscription.VALIDE),
                new EnrollmentSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", StatutInscription.VALIDE),
                new EnrollmentSeed("meriem.growth@gmail.com", "Social media manager starter", StatutInscription.VALIDE),
                new EnrollmentSeed("meriem.growth@gmail.com", "Growth marketing & analytics", StatutInscription.EN_ATTENTE),
                new EnrollmentSeed("walid.student@gmail.com", "Cyber hygiene au quotidien", StatutInscription.VALIDE),
                new EnrollmentSeed("walid.student@gmail.com", "Securiser une application web moderne", StatutInscription.VALIDE),
                new EnrollmentSeed("lina.career@gmail.com", "Power BI & reporting metier", StatutInscription.VALIDE),
                new EnrollmentSeed("lina.career@gmail.com", "Design system pour applications web", StatutInscription.VALIDE),
                new EnrollmentSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", StatutInscription.VALIDE),
                new EnrollmentSeed("lina.career@gmail.com", "Machine learning pour debutants", StatutInscription.EN_ATTENTE)
        );

        Map<String, Inscription> inscriptions = new LinkedHashMap<>();
        for (EnrollmentSeed seed : enrollmentSeeds) {
            Etudiant student = demoUsers.student(seed.studentEmail());
            Cours course = demoCatalog.course(seed.courseTitle());
            Inscription inscription = findOrCreateInscription(
                    inscriptionRepository,
                    student,
                    course,
                    seed.status()
            );
            inscriptions.put(composeKey(seed.studentEmail(), seed.courseTitle()), inscription);
        }

        return new DemoAcademy(inscriptions);
    }

    private void createDemoReviews(AvisCoursRepository avisCoursRepository,
                                   DemoUsers demoUsers,
                                   DemoCatalog demoCatalog) {
        List<ReviewSeed> reviewSeeds = List.of(
                new ReviewSeed("emna@gmail.com", "Angular 21 de zero a projet reel", 5,
                        "Parcours clair, moderne et tres utile pour connecter Angular a un vrai backend."),
                new ReviewSeed("emna@gmail.com", "Spring Boot API securisee", 5,
                        "Bon equilibre entre securite, API REST et structure projet."),
                new ReviewSeed("emna@gmail.com", "SQL & dashboards pour debutants", 4,
                        "Tres bon point de depart pour comprendre les KPIs et le reporting."),
                new ReviewSeed("aya.etudiante@gmail.com", "Angular 21 de zero a projet reel", 5,
                        "Interface bien expliquee et progression facile a suivre."),
                new ReviewSeed("aya.etudiante@gmail.com", "UX research appliquee au produit", 5,
                        "Excellent cours pour structurer interviews, syntheses et hypotheses."),
                new ReviewSeed("aya.etudiante@gmail.com", "Design system pour applications web", 5,
                        "Super utile pour harmoniser composants, tokens et spacing."),
                new ReviewSeed("aya.etudiante@gmail.com", "Figma to code pour equipes produit", 4,
                        "La transition maquette vers code est bien demystifiee."),
                new ReviewSeed("yassine.product@gmail.com", "Gestion de projet agile pour juniors", 4,
                        "Simple, concret et facile a remettre en pratique dans un projet equipe."),
                new ReviewSeed("yassine.product@gmail.com", "Communication professionnelle & presentations", 5,
                        "Tres bon cours pour mieux presenter et mieux formuler ses idees."),
                new ReviewSeed("meriem.growth@gmail.com", "SQL & dashboards pour debutants", 5,
                        "Les exemples sont parlants et le cours donne confiance."),
                new ReviewSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", 5,
                        "Parfait pour comprendre les bases SEO sans jargon inutile."),
                new ReviewSeed("meriem.growth@gmail.com", "Social media manager starter", 4,
                        "Bonne base pour organiser son contenu et suivre ses performances."),
                new ReviewSeed("walid.student@gmail.com", "Cyber hygiene au quotidien", 4,
                        "Court, accessible et tres utile au quotidien."),
                new ReviewSeed("walid.student@gmail.com", "Securiser une application web moderne", 5,
                        "Approche tres concrete des sujets auth, JWT et permissions."),
                new ReviewSeed("lina.career@gmail.com", "Power BI & reporting metier", 4,
                        "Le cours aide a structurer les tableaux de bord de maniere lisible."),
                new ReviewSeed("lina.career@gmail.com", "Design system pour applications web", 4,
                        "Bon complement pour mieux comprendre les standards UI."),
                new ReviewSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", 5,
                        "Pratique et rassurant pour les calls, emails et presentions client.")
        );

        for (ReviewSeed seed : reviewSeeds) {
            findOrCreateReview(
                    avisCoursRepository,
                    demoUsers.student(seed.studentEmail()),
                    demoCatalog.course(seed.courseTitle()),
                    seed.note(),
                    seed.comment()
            );
        }
    }

    private void createDemoPayments(PaiementCoursRepository paiementCoursRepository,
                                    DemoAcademy demoAcademy,
                                    DemoCatalog demoCatalog) {
        List<PaymentSeed> paymentSeeds = List.of(
                new PaymentSeed("emna@gmail.com", "Angular 21 de zero a projet reel", "PAY-ANG-001", StatutPaiement.APPROUVE),
                new PaymentSeed("emna@gmail.com", "Spring Boot API securisee", "PAY-SPR-001", StatutPaiement.APPROUVE),
                new PaymentSeed("emna@gmail.com", "SQL & dashboards pour debutants", "PAY-SQL-001", StatutPaiement.APPROUVE),
                new PaymentSeed("emna@gmail.com", "Angular & Spring Boot full stack", "PAY-FULL-001", StatutPaiement.EN_ATTENTE),
                new PaymentSeed("aya.etudiante@gmail.com", "Angular 21 de zero a projet reel", "PAY-ANG-002", StatutPaiement.APPROUVE),
                new PaymentSeed("aya.etudiante@gmail.com", "UX research appliquee au produit", "PAY-UX-001", StatutPaiement.APPROUVE),
                new PaymentSeed("aya.etudiante@gmail.com", "Design system pour applications web", "PAY-DS-001", StatutPaiement.APPROUVE),
                new PaymentSeed("aya.etudiante@gmail.com", "Figma to code pour equipes produit", "PAY-FIG-001", StatutPaiement.APPROUVE),
                new PaymentSeed("yassine.product@gmail.com", "Gestion de projet agile pour juniors", "PAY-AGI-001", StatutPaiement.APPROUVE),
                new PaymentSeed("yassine.product@gmail.com", "Communication professionnelle & presentations", "PAY-COM-001", StatutPaiement.APPROUVE),
                new PaymentSeed("yassine.product@gmail.com", "Angular & Spring Boot full stack", "PAY-FULL-002", StatutPaiement.REFUSE),
                new PaymentSeed("meriem.growth@gmail.com", "SQL & dashboards pour debutants", "PAY-SQL-002", StatutPaiement.APPROUVE),
                new PaymentSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", "PAY-SEO-001", StatutPaiement.APPROUVE),
                new PaymentSeed("meriem.growth@gmail.com", "Social media manager starter", "PAY-SOC-001", StatutPaiement.APPROUVE),
                new PaymentSeed("meriem.growth@gmail.com", "Growth marketing & analytics", "PAY-GRO-001", StatutPaiement.EN_ATTENTE),
                new PaymentSeed("walid.student@gmail.com", "Securiser une application web moderne", "PAY-SEC-001", StatutPaiement.APPROUVE),
                new PaymentSeed("lina.career@gmail.com", "Power BI & reporting metier", "PAY-PBI-001", StatutPaiement.APPROUVE),
                new PaymentSeed("lina.career@gmail.com", "Design system pour applications web", "PAY-DS-002", StatutPaiement.APPROUVE),
                new PaymentSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", "PAY-ENG-001", StatutPaiement.APPROUVE),
                new PaymentSeed("lina.career@gmail.com", "Machine learning pour debutants", "PAY-ML-001", StatutPaiement.EN_ATTENTE)
        );

        for (PaymentSeed seed : paymentSeeds) {
            Inscription inscription = demoAcademy.inscription(seed.studentEmail(), seed.courseTitle());
            Cours course = demoCatalog.course(seed.courseTitle());
            if (course.getPrix() == null || course.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            findOrCreatePayment(
                    paiementCoursRepository,
                    inscription,
                    seed.code(),
                    seed.status()
            );
        }
    }

    private void createDemoProgressions(ProgressionLeconRepository progressionLeconRepository,
                                        DemoUsers demoUsers,
                                        DemoLearning demoLearning) {
        List<ProgressSeed> progressSeeds = List.of(
                new ProgressSeed("emna@gmail.com", "Angular 21 de zero a projet reel", 3),
                new ProgressSeed("emna@gmail.com", "Spring Boot API securisee", 2),
                new ProgressSeed("emna@gmail.com", "SQL & dashboards pour debutants", 2),
                new ProgressSeed("aya.etudiante@gmail.com", "UX research appliquee au produit", 2),
                new ProgressSeed("aya.etudiante@gmail.com", "Design system pour applications web", 3),
                new ProgressSeed("aya.etudiante@gmail.com", "Figma to code pour equipes produit", 2),
                new ProgressSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", 3),
                new ProgressSeed("meriem.growth@gmail.com", "Social media manager starter", 2),
                new ProgressSeed("walid.student@gmail.com", "Cyber hygiene au quotidien", 3),
                new ProgressSeed("walid.student@gmail.com", "Securiser une application web moderne", 3),
                new ProgressSeed("lina.career@gmail.com", "Power BI & reporting metier", 2),
                new ProgressSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", 3)
        );

        for (ProgressSeed seed : progressSeeds) {
            Etudiant student = demoUsers.student(seed.studentEmail());
            List<Lecon> lessons = demoLearning.lessons(seed.courseTitle());
            int limit = Math.min(seed.completedLessons(), lessons.size());
            for (int index = 0; index < limit; index++) {
                findOrCreateProgression(progressionLeconRepository, student, lessons.get(index));
            }
        }
    }

    private void createDemoResults(ResultatEvaluationRepository resultatEvaluationRepository,
                                   DemoUsers demoUsers,
                                   DemoLearning demoLearning) {
        List<ResultSeed> resultSeeds = List.of(
                new ResultSeed("emna@gmail.com", "Angular 21 de zero a projet reel", 10, 10, 20.0, true, 1),
                new ResultSeed("emna@gmail.com", "Spring Boot API securisee", 7, 10, 14.0, true, 1),
                new ResultSeed("emna@gmail.com", "SQL & dashboards pour debutants", 6, 10, 12.0, true, 1),
                new ResultSeed("aya.etudiante@gmail.com", "UX research appliquee au produit", 8, 10, 16.0, true, 1),
                new ResultSeed("aya.etudiante@gmail.com", "Design system pour applications web", 10, 10, 20.0, true, 1),
                new ResultSeed("aya.etudiante@gmail.com", "Figma to code pour equipes produit", 7, 10, 14.0, true, 1),
                new ResultSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", 10, 10, 20.0, true, 1),
                new ResultSeed("meriem.growth@gmail.com", "Social media manager starter", 7, 10, 14.0, true, 1),
                new ResultSeed("walid.student@gmail.com", "Cyber hygiene au quotidien", 9, 10, 18.0, true, 1),
                new ResultSeed("walid.student@gmail.com", "Securiser une application web moderne", 10, 10, 20.0, true, 1),
                new ResultSeed("lina.career@gmail.com", "Power BI & reporting metier", 6, 10, 12.0, true, 1),
                new ResultSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", 9, 10, 18.0, true, 1)
        );

        for (ResultSeed seed : resultSeeds) {
            Evaluation evaluation = demoLearning.quiz(seed.courseTitle());
            Etudiant student = demoUsers.student(seed.studentEmail());
            findOrCreateResult(
                    resultatEvaluationRepository,
                    evaluation,
                    student,
                    seed.score(),
                    seed.totalPoints(),
                    seed.noteObtenue(),
                    seed.reussi(),
                    seed.tentativeNumero()
            );
        }
    }

    private void createDemoCertificates(CertificatRepository certificatRepository,
                                        DemoUsers demoUsers,
                                        DemoCatalog demoCatalog) {
        List<CertificateSeed> certificateSeeds = List.of(
                new CertificateSeed("emna@gmail.com", "Angular 21 de zero a projet reel", "EDU-DEMO-ANG-001"),
                new CertificateSeed("aya.etudiante@gmail.com", "Design system pour applications web", "EDU-DEMO-DS-001"),
                new CertificateSeed("meriem.growth@gmail.com", "SEO pratique pour lancer un site", "EDU-DEMO-SEO-001"),
                new CertificateSeed("walid.student@gmail.com", "Securiser une application web moderne", "EDU-DEMO-SEC-001"),
                new CertificateSeed("lina.career@gmail.com", "Anglais professionnel pour freelances", "EDU-DEMO-ENG-001")
        );

        for (CertificateSeed seed : certificateSeeds) {
            findOrCreateCertificate(
                    certificatRepository,
                    demoUsers.student(seed.studentEmail()),
                    demoCatalog.course(seed.courseTitle()),
                    seed.code()
            );
        }
    }

    private Formateur findOrCreateTeacher(UtilisateurRepository utilisateurRepository,
                                          FormateurRepository formateurRepository,
                                          PasswordEncoder passwordEncoder,
                                          String nom,
                                          String email,
                                          String password,
                                          String status,
                                          String specialite,
                                          String portfolio,
                                          String bio,
                                          String motivation) {
        return (Formateur) utilisateurRepository.findByEmail(email).orElseGet(() -> {
            Formateur formateur = new Formateur();
            formateur.setNom(nom);
            formateur.setEmail(email);
            formateur.setMotDePasse(passwordEncoder.encode(password));
            formateur.setRole(Role.FORMATEUR);
            formateur.setStatus(status);
            formateur.setSpecialite(specialite);
            formateur.setPortfolio(portfolio);
            formateur.setBio(bio);
            formateur.setMotivation(motivation);
            formateur.setCommentaireAdmin("Profil cree automatiquement pour alimenter la plateforme.");
            return formateurRepository.save(formateur);
        });
    }

    private Etudiant findOrCreateStudent(UtilisateurRepository utilisateurRepository,
                                         EtudiantRepository etudiantRepository,
                                         PasswordEncoder passwordEncoder,
                                         String nom,
                                         String email,
                                         String password) {
        return (Etudiant) utilisateurRepository.findByEmail(email).orElseGet(() -> {
            Etudiant etudiant = new Etudiant();
            etudiant.setNom(nom);
            etudiant.setEmail(email);
            etudiant.setMotDePasse(passwordEncoder.encode(password));
            etudiant.setRole(Role.ETUDIANT);
            etudiant.setStatus("ACTIVE");
            return etudiantRepository.save(etudiant);
        });
    }

    private Categorie findOrCreateCategory(CategorieRepository categorieRepository,
                                           String nom,
                                           String description) {
        return categorieRepository.findAll().stream()
                .filter(category -> nom.equalsIgnoreCase(category.getNom()))
                .findFirst()
                .orElseGet(() -> categorieRepository.save(new Categorie(null, nom, description, null)));
    }

    private SousCategorie findOrCreateSubCategory(SousCategorieRepository sousCategorieRepository,
                                                  String nom,
                                                  String description,
                                                  Categorie categorie) {
        return sousCategorieRepository.findByCategorieId(categorie.getId()).stream()
                .filter(subCategory -> nom.equalsIgnoreCase(subCategory.getNom()))
                .findFirst()
                .orElseGet(() -> {
                    SousCategorie sousCategorie = new SousCategorie();
                    sousCategorie.setNom(nom);
                    sousCategorie.setDescription(description);
                    sousCategorie.setCategorie(categorie);
                    return sousCategorieRepository.save(sousCategorie);
                });
    }

    private Cours findOrCreateCourse(CoursRepository coursRepository,
                                     CourseSeed seed,
                                     Formateur teacher,
                                     SousCategorie subCategory) {
        return coursRepository.findAll().stream()
                .filter(course -> seed.title().equalsIgnoreCase(course.getTitre()))
                .findFirst()
                .orElseGet(() -> {
                    Cours cours = new Cours();
                    cours.setTitre(seed.title());
                    cours.setDescription(seed.description());
                    cours.setFormateur(teacher);
                    cours.setSousCategorie(subCategory);
                    cours.setDuree(seed.duration());
                    cours.setNiveau(seed.level());
                    cours.setImageUrl(seed.imageUrl());
                    cours.setVideoUrl("https://www.youtube.com/watch?v=3dHNOWTI7H8");
                    cours.setPdfUrl("/uploads/demo-support.pdf");
                    cours.setPrix(seed.price());
                    cours.setEtatPublication(seed.state());
                    if (seed.state() == EtatCours.PUBLIE) {
                        cours.setDatePublication(LocalDateTime.now().minusDays(seed.publicationLagDays()));
                    }
                    return coursRepository.save(cours);
                });
    }

    private Lecon findOrCreateLesson(LeconRepository leconRepository,
                                     Cours cours,
                                     int ordre,
                                     String titre,
                                     String description) {
        return leconRepository.findAll().stream()
                .filter(lecon -> lecon.getCours().getId().equals(cours.getId())
                        && ordre == lecon.getOrdre()
                        && titre.equalsIgnoreCase(lecon.getTitre()))
                .findFirst()
                .orElseGet(() -> {
                    Lecon lecon = new Lecon();
                    lecon.setOrdre(ordre);
                    lecon.setTitre(titre);
                    lecon.setDescription(description);
                    lecon.setContenuHtml("<p>Module de demonstration pour enrichir le parcours " + cours.getTitre() + ".</p>");
                    lecon.setCours(cours);
                    return leconRepository.save(lecon);
                });
    }

    private Evaluation createQuizWithQuestion(EvaluationRepository evaluationRepository,
                                              QuestionRepository questionRepository,
                                              ChoixRepository choixRepository,
                                              Lecon lecon,
                                              String quizTitle,
                                              String questionText,
                                              String correctChoice,
                                              List<String> wrongChoices) {
        Evaluation evaluation = evaluationRepository.findAll().stream()
                .filter(item -> item.getLecon().getId().equals(lecon.getId())
                        && quizTitle.equalsIgnoreCase(item.getTitre()))
                .findFirst()
                .orElseGet(() -> {
                    Evaluation quiz = new Evaluation();
                    quiz.setTitre(quizTitle);
                    quiz.setType(TypeEvaluation.QUIZ);
                    quiz.setNoteMax(20);
                    quiz.setNoteMin(12);
                    quiz.setPublie(true);
                    quiz.setLecon(lecon);
                    return evaluationRepository.save(quiz);
                });

        if (!Boolean.TRUE.equals(evaluation.getPublie())) {
            evaluation.setPublie(true);
            evaluationRepository.save(evaluation);
        }

        Question question = questionRepository.findAll().stream()
                .filter(item -> item.getEvaluation().getId().equals(evaluation.getId())
                        && questionText.equalsIgnoreCase(item.getEnonce()))
                .findFirst()
                .orElseGet(() -> {
                    Question created = new Question();
                    created.setEnonce(questionText);
                    created.setPoint(10);
                    created.setEvaluation(evaluation);
                    return questionRepository.save(created);
                });

        findOrCreateChoice(choixRepository, question, correctChoice, true);
        for (String wrongChoice : wrongChoices) {
            findOrCreateChoice(choixRepository, question, wrongChoice, false);
        }

        return evaluation;
    }

    private void findOrCreateChoice(ChoixRepository choixRepository,
                                    Question question,
                                    String text,
                                    boolean correct) {
        boolean exists = choixRepository.findAll().stream()
                .anyMatch(choice -> choice.getQuestion().getId().equals(question.getId())
                        && text.equalsIgnoreCase(choice.getTexte()));
        if (exists) {
            return;
        }

        Choix choix = new Choix();
        choix.setTexte(text);
        choix.setEstCorrect(correct);
        choix.setQuestion(question);
        choixRepository.save(choix);
    }

    private Inscription findOrCreateInscription(InscriptionRepository inscriptionRepository,
                                                Etudiant etudiant,
                                                Cours cours,
                                                StatutInscription statut) {
        Inscription inscription = inscriptionRepository.findByEtudiantIdAndCoursId(etudiant.getId(), cours.getId())
                .orElseGet(() -> {
                    Inscription created = new Inscription();
                    created.setEtudiant(etudiant);
                    created.setCours(cours);
                    return created;
                });
        inscription.setStatut(statut);
        return inscriptionRepository.save(inscription);
    }

    private void findOrCreateReview(AvisCoursRepository avisCoursRepository,
                                    Etudiant etudiant,
                                    Cours cours,
                                    int note,
                                    String commentaire) {
        boolean exists = avisCoursRepository.findAll().stream()
                .anyMatch(review -> review.getEtudiant().getId().equals(etudiant.getId())
                        && review.getCours().getId().equals(cours.getId()));
        if (exists) {
            return;
        }

        AvisCours avisCours = new AvisCours();
        avisCours.setEtudiant(etudiant);
        avisCours.setCours(cours);
        avisCours.setNote(note);
        avisCours.setCommentaire(commentaire);
        avisCoursRepository.save(avisCours);
    }

    private void findOrCreatePayment(PaiementCoursRepository paiementCoursRepository,
                                     Inscription inscription,
                                     String code,
                                     StatutPaiement statut) {
        boolean exists = paiementCoursRepository.findAll().stream()
                .anyMatch(payment -> code.equalsIgnoreCase(payment.getCodePaiement()));
        if (exists) {
            return;
        }

        BigDecimal amount = inscription.getCours().getPrix().setScale(2, RoundingMode.HALF_UP);
        PaiementCours paiement = new PaiementCours();
        paiement.setCodePaiement(code);
        paiement.setMontant(amount);
        paiement.setCommissionPlateforme(computePlatformShare(amount));
        paiement.setMontantFormateur(computeTeacherShare(amount));
        paiement.setStatut(statut);
        paiement.setEtudiant(inscription.getEtudiant());
        paiement.setCours(inscription.getCours());
        paiement.setInscription(inscription);
        if (statut != StatutPaiement.EN_ATTENTE) {
            paiement.setDateDecision(LocalDateTime.now().minusDays(1));
        }
        paiementCoursRepository.save(paiement);
    }

    private void findOrCreateProgression(ProgressionLeconRepository progressionLeconRepository,
                                         Etudiant etudiant,
                                         Lecon lecon) {
        boolean exists = progressionLeconRepository.findAll().stream()
                .anyMatch(progression -> progression.getEtudiant().getId().equals(etudiant.getId())
                        && progression.getLecon().getId().equals(lecon.getId()));
        if (exists) {
            return;
        }

        ProgressionLecon progression = new ProgressionLecon();
        progression.setEtudiant(etudiant);
        progression.setLecon(lecon);
        progressionLeconRepository.save(progression);
    }

    private void findOrCreateResult(ResultatEvaluationRepository resultatEvaluationRepository,
                                    Evaluation evaluation,
                                    Etudiant etudiant,
                                    int score,
                                    int totalPoints,
                                    double noteObtenue,
                                    boolean reussi,
                                    int tentativeNumero) {
        boolean exists = resultatEvaluationRepository.findAll().stream()
                .anyMatch(result -> result.getEvaluation().getId().equals(evaluation.getId())
                        && result.getEtudiant().getId().equals(etudiant.getId())
                        && tentativeNumero == result.getTentativeNumero());
        if (exists) {
            return;
        }

        ResultatEvaluation result = new ResultatEvaluation();
        result.setEvaluation(evaluation);
        result.setEtudiant(etudiant);
        result.setScore(score);
        result.setTotalPoints(totalPoints);
        result.setPourcentage((int) Math.round(score * 100.0 / Math.max(totalPoints, 1)));
        result.setNoteObtenue(noteObtenue);
        result.setReussi(reussi);
        result.setTentativeNumero(tentativeNumero);
        resultatEvaluationRepository.save(result);
    }

    private void findOrCreateCertificate(CertificatRepository certificatRepository,
                                         Etudiant etudiant,
                                         Cours cours,
                                         String code) {
        boolean exists = certificatRepository.findAll().stream()
                .anyMatch(certificat -> certificat.getEtudiant().getId().equals(etudiant.getId())
                        && certificat.getCours().getId().equals(cours.getId()));
        if (exists) {
            return;
        }

        Certificat certificat = new Certificat();
        certificat.setCode(code);
        certificat.setEtudiant(etudiant);
        certificat.setCours(cours);
        certificat.setUrlPdf("/api/certificats/demo/" + code.toLowerCase(Locale.ROOT) + ".pdf");
        certificatRepository.save(certificat);
    }

    private BigDecimal computePlatformShare(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeTeacherShare(BigDecimal amount) {
        return amount.subtract(computePlatformShare(amount)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String composeKey(String studentEmail, String courseTitle) {
        return studentEmail.toLowerCase(Locale.ROOT) + "|" + courseTitle.toLowerCase(Locale.ROOT);
    }

    private record DemoUsers(Map<String, Formateur> teachers,
                             Map<String, Etudiant> students,
                             Formateur pendingTeacher) {

        Formateur teacher(String email) {
            Formateur teacher = teachers.get(email);
            if (teacher == null) {
                throw new IllegalStateException("Formateur demo introuvable: " + email);
            }
            return teacher;
        }

        Etudiant student(String email) {
            Etudiant student = students.get(email);
            if (student == null) {
                throw new IllegalStateException("Etudiant demo introuvable: " + email);
            }
            return student;
        }
    }

    private record DemoCatalog(Map<String, Cours> courses) {
        Cours course(String title) {
            Cours course = courses.get(title);
            if (course == null) {
                throw new IllegalStateException("Cours demo introuvable: " + title);
            }
            return course;
        }
    }

    private record DemoLearning(Map<String, List<Lecon>> lessonsByCourse,
                                Map<String, Evaluation> quizzesByCourse) {
        List<Lecon> lessons(String courseTitle) {
            List<Lecon> lessons = lessonsByCourse.get(courseTitle);
            if (lessons == null) {
                throw new IllegalStateException("Lecons demo introuvables: " + courseTitle);
            }
            return lessons;
        }

        Evaluation quiz(String courseTitle) {
            Evaluation evaluation = quizzesByCourse.get(courseTitle);
            if (evaluation == null) {
                throw new IllegalStateException("Quiz demo introuvable: " + courseTitle);
            }
            return evaluation;
        }
    }

    private record DemoAcademy(Map<String, Inscription> inscriptions) {
        Inscription inscription(String studentEmail, String courseTitle) {
            Inscription inscription = inscriptions.get(
                    studentEmail.toLowerCase(Locale.ROOT) + "|" + courseTitle.toLowerCase(Locale.ROOT)
            );
            if (inscription == null) {
                throw new IllegalStateException("Inscription demo introuvable: " + studentEmail + " / " + courseTitle);
            }
            return inscription;
        }
    }

    private record CourseSeed(String title,
                              String description,
                              String teacherEmail,
                              String subCategoryName,
                              String duration,
                              String level,
                              String imageUrl,
                              BigDecimal price,
                              EtatCours state,
                              int publicationLagDays) {
    }

    private record EnrollmentSeed(String studentEmail,
                                  String courseTitle,
                                  StatutInscription status) {
    }

    private record ReviewSeed(String studentEmail,
                              String courseTitle,
                              int note,
                              String comment) {
    }

    private record PaymentSeed(String studentEmail,
                               String courseTitle,
                               String code,
                               StatutPaiement status) {
    }

    private record ProgressSeed(String studentEmail,
                                String courseTitle,
                                int completedLessons) {
    }

    private record ResultSeed(String studentEmail,
                              String courseTitle,
                              int score,
                              int totalPoints,
                              double noteObtenue,
                              boolean reussi,
                              int tentativeNumero) {
    }

    private record CertificateSeed(String studentEmail,
                                   String courseTitle,
                                   String code) {
    }
}
